package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyBox;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyMovement;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcUserMoneyRepository implements UserMoneyRepository {
    private static final int SCALE = 2;

    private final JdbcTemplate jdbcTemplate;
    private final AppUserRepository appUserRepository;

    @Override
    public UserMoneyMovement addMovement(Long userId, BigDecimal amount, String description, Long currentWeekMenuId) {
        appUserRepository.findById(userId);
        BigDecimal scaledAmount = scale(amount);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO user_money_movements (user_id, amount, description, current_week_menu_id)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, userId);
            ps.setBigDecimal(2, scaledAmount);
            ps.setString(3, description);
            if (currentWeekMenuId == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, currentWeekMenuId);
            }
            return ps;
        }, keyHolder);
        return UserMoneyMovement.builder()
                .id(keyHolder.getKey().longValue())
                .userId(userId)
                .amount(scaledAmount)
                .description(description)
                .currentWeekMenuId(currentWeekMenuId)
                .createdAt(loadCreatedAt(keyHolder.getKey().longValue()))
                .build();
    }

    @Override
    public UserMoneyBox findMoneyBox(Long userId) {
        AppUser user = appUserRepository.findById(userId);
        List<UserMoneyMovement> movements = jdbcTemplate.query("""
                        SELECT id, user_id, amount, description, current_week_menu_id, created_at
                        FROM user_money_movements
                        WHERE user_id = ?
                        ORDER BY created_at DESC, id DESC
                        """,
                movementRowMapper(),
                userId
        );
        BigDecimal balance = movements.stream()
                .map(UserMoneyMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return UserMoneyBox.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .balance(scale(balance))
                .movements(movements)
                .build();
    }

    private Instant loadCreatedAt(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT created_at FROM user_money_movements WHERE id = ?",
                Instant.class,
                id
        );
    }

    private RowMapper<UserMoneyMovement> movementRowMapper() {
        return (rs, rowNum) -> UserMoneyMovement.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .amount(scale(rs.getBigDecimal("amount")))
                .description(rs.getString("description"))
                .currentWeekMenuId(rs.getObject("current_week_menu_id", Long.class))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, java.math.RoundingMode.HALF_UP);
    }
}
