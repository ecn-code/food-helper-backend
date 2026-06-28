package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.UserWeightEntry;
import com.eliascanalesnieto.foodhelper.domain.UserWeightRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcUserWeightRepository implements UserWeightRepository {
    private final JdbcTemplate jdbcTemplate;
    private final AppUserRepository appUserRepository;

    @Override
    public UserWeightEntry create(Long userId, BigDecimal weight, Instant recordedAt) {
        appUserRepository.findById(userId);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO user_weight_entries (user_id, weight, recorded_at) VALUES (?, ?, ?)",
                    new String[]{"id"}
            );
            statement.setLong(1, userId);
            statement.setBigDecimal(2, weight);
            statement.setTimestamp(3, Timestamp.from(recordedAt));
            return statement;
        }, keyHolder);
        return UserWeightEntry.builder()
                .id(keyHolder.getKey().longValue())
                .userId(userId)
                .weight(weight)
                .recordedAt(recordedAt)
                .build();
    }

    @Override
    public UserWeightEntry update(Long userId, Long weightId, BigDecimal weight, Instant recordedAt) {
        int updatedRows = jdbcTemplate.update("""
                        UPDATE user_weight_entries
                        SET weight = ?, recorded_at = ?
                        WHERE id = ? AND user_id = ?
                        """,
                weight, Timestamp.from(recordedAt), weightId, userId);
        if (updatedRows == 0) {
            throw weightNotFound(weightId, userId);
        }
        return UserWeightEntry.builder()
                .id(weightId)
                .userId(userId)
                .weight(weight)
                .recordedAt(recordedAt)
                .build();
    }

    @Override
    public void delete(Long userId, Long weightId) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM user_weight_entries WHERE id = ? AND user_id = ?",
                weightId,
                userId
        );
        if (deletedRows == 0) {
            throw weightNotFound(weightId, userId);
        }
    }

    @Override
    public List<UserWeightEntry> findByPeriod(Long userId, Instant from, Instant to) {
        appUserRepository.findById(userId);
        return jdbcTemplate.query("""
                        SELECT id, user_id, weight, recorded_at
                        FROM user_weight_entries
                        WHERE user_id = ? AND recorded_at >= ? AND recorded_at <= ?
                        ORDER BY recorded_at ASC, id ASC
                        """,
                (resultSet, rowNum) -> UserWeightEntry.builder()
                        .id(resultSet.getLong("id"))
                        .userId(resultSet.getLong("user_id"))
                        .weight(resultSet.getBigDecimal("weight"))
                        .recordedAt(resultSet.getTimestamp("recorded_at").toInstant())
                        .build(),
                userId, Timestamp.from(from), Timestamp.from(to));
    }

    private ResourceNotFoundException weightNotFound(Long weightId, Long userId) {
        return new ResourceNotFoundException(
                "Weight measurement " + weightId + " was not found for user " + userId
        );
    }
}
