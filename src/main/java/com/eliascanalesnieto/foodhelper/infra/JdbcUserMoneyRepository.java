package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.MoneyBox;
import com.eliascanalesnieto.foodhelper.domain.MoneyBoxType;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyBox;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyMovement;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        Long moneyBoxId = jdbcTemplate.queryForObject(
                "SELECT id FROM money_boxes WHERE user_id = ?",
                Long.class,
                userId
        );
        return insertMovement(moneyBoxId, userId, amount, description, currentWeekMenuId);
    }

    @Override
    public MoneyBox createManualMoneyBox(String name) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO money_boxes (name) VALUES (?)",
                        new String[]{"id"}
                );
                ps.setString(1, name);
                return ps;
            }, keyHolder);
            return findMoneyBoxById(keyHolder.getKey().longValue());
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Manual money box name already exists");
        }
    }

    @Override
    public List<MoneyBox> findAllMoneyBoxes() {
        return jdbcTemplate.queryForList("""
                        SELECT id
                        FROM money_boxes
                        ORDER BY CASE WHEN user_id IS NOT NULL THEN 0 ELSE 1 END, LOWER(name), id
                        """, Long.class).stream()
                .map(this::findMoneyBoxById)
                .toList();
    }

    @Override
    public MoneyBox findMoneyBoxById(Long moneyBoxId) {
        List<MoneyBox> moneyBoxes = jdbcTemplate.query("""
                        SELECT money_box.id, money_box.name, money_box.user_id, app_user.username
                        FROM money_boxes money_box
                        LEFT JOIN app_users app_user ON app_user.id = money_box.user_id
                        WHERE money_box.id = ?
                        """,
                (rs, rowNum) -> {
                    Long userId = rs.getObject("user_id", Long.class);
                    List<UserMoneyMovement> movements = findMovementsByMoneyBoxId(moneyBoxId);
                    BigDecimal balance = movements.stream()
                            .map(UserMoneyMovement::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return MoneyBox.builder()
                            .id(rs.getLong("id"))
                            .type(userId == null ? MoneyBoxType.MANUAL : MoneyBoxType.USER)
                            .name(rs.getString("name"))
                            .userId(userId)
                            .username(rs.getString("username"))
                            .balance(scale(balance))
                            .movements(movements)
                            .build();
                },
                moneyBoxId
        );
        if (moneyBoxes.isEmpty()) {
            throw new ResourceNotFoundException("Money box not found");
        }
        return moneyBoxes.getFirst();
    }

    @Override
    public UserMoneyMovement addMoneyBoxMovement(Long moneyBoxId, BigDecimal amount, String description) {
        MoneyBox moneyBox = findMoneyBoxById(moneyBoxId);
        return insertMovement(moneyBoxId, moneyBox.getUserId(), amount, description, null);
    }

    @Override
    public void deleteMoneyBox(Long moneyBoxId) {
        jdbcTemplate.update("DELETE FROM user_money_movements WHERE money_box_id = ?", moneyBoxId);
        int deleted = jdbcTemplate.update("DELETE FROM money_boxes WHERE id = ?", moneyBoxId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Money box not found");
        }
    }

    @Override
    public void deleteMoneyBoxMovement(Long moneyBoxId, Long movementId) {
        List<Boolean> linkedToMenu = jdbcTemplate.query(
                """
                SELECT current_week_menu_id IS NOT NULL
                FROM user_money_movements
                WHERE id = ? AND money_box_id = ?
                """,
                (rs, rowNum) -> rs.getBoolean(1),
                movementId,
                moneyBoxId
        );
        if (linkedToMenu.isEmpty()) {
            throw new ResourceNotFoundException("Money movement not found");
        }
        if (linkedToMenu.getFirst()) {
            throw new DuplicateResourceException("Menu-linked money movement cannot be deleted");
        }
        jdbcTemplate.update(
                "DELETE FROM user_money_movements WHERE id = ? AND money_box_id = ?",
                movementId,
                moneyBoxId
        );
    }

    @Override
    public void deleteMovementsByCurrentWeekMenuId(Long currentWeekMenuId) {
        jdbcTemplate.update(
                "DELETE FROM user_money_movements WHERE current_week_menu_id = ?",
                currentWeekMenuId
        );
    }

    private UserMoneyMovement insertMovement(
            Long moneyBoxId,
            Long userId,
            BigDecimal amount,
            String description,
            Long currentWeekMenuId
    ) {
        BigDecimal scaledAmount = scale(amount);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO user_money_movements (money_box_id, user_id, amount, description, current_week_menu_id)
                    VALUES (?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, moneyBoxId);
            if (userId == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, userId);
            }
            ps.setBigDecimal(3, scaledAmount);
            ps.setString(4, description);
            if (currentWeekMenuId == null) {
                ps.setNull(5, Types.BIGINT);
            } else {
                ps.setLong(5, currentWeekMenuId);
            }
            return ps;
        }, keyHolder);
        return UserMoneyMovement.builder()
                .id(keyHolder.getKey().longValue())
                .moneyBoxId(moneyBoxId)
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
        MoneyBox moneyBox = jdbcTemplate.queryForObject(
                "SELECT id FROM money_boxes WHERE user_id = ?",
                (rs, rowNum) -> findMoneyBoxById(rs.getLong("id")),
                userId
        );
        return UserMoneyBox.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .balance(moneyBox.getBalance())
                .movements(moneyBox.getMovements())
                .build();
    }

    private List<UserMoneyMovement> findMovementsByMoneyBoxId(Long moneyBoxId) {
        return jdbcTemplate.query("""
                        SELECT id, money_box_id, user_id, amount, description, current_week_menu_id, created_at
                        FROM user_money_movements
                        WHERE money_box_id = ?
                        ORDER BY created_at DESC, id DESC
                        """,
                movementRowMapper(),
                moneyBoxId
        );
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
                .moneyBoxId(rs.getLong("money_box_id"))
                .userId(rs.getObject("user_id", Long.class))
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
