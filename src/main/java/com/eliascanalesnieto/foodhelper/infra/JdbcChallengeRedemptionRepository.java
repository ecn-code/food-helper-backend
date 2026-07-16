package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.ChallengeRedemption;
import com.eliascanalesnieto.foodhelper.domain.ChallengeRedemptionRepository;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcChallengeRedemptionRepository implements ChallengeRedemptionRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ChallengeRedemption> findLatestByUserIdAndChallengeCode(Long userId, String challengeCode) {
        List<ChallengeRedemption> redemptions = jdbcTemplate.query("""
                        SELECT id, user_id, challenge_code, reward_amount, used_at
                        FROM challenge_redemptions
                        WHERE user_id = ? AND challenge_code = ?
                        ORDER BY used_at DESC, id DESC
                        LIMIT 1
                        """,
                rowMapper(), userId, challengeCode);
        return redemptions.stream().findFirst();
    }

    @Override
    public ChallengeRedemption save(ChallengeRedemption redemption) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO challenge_redemptions (user_id, challenge_code, reward_amount, used_at)
                    VALUES (?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, redemption.getUserId());
            ps.setString(2, redemption.getChallengeCode());
            ps.setBigDecimal(3, scale(redemption.getRewardAmount()));
            ps.setTimestamp(4, java.sql.Timestamp.from(redemption.getUsedAt()));
            return ps;
        }, keyHolder);
        return redemption.toBuilder().id(keyHolder.getKey().longValue()).build();
    }

    private RowMapper<ChallengeRedemption> rowMapper() {
        return (rs, rowNum) -> ChallengeRedemption.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .challengeCode(rs.getString("challenge_code"))
                .rewardAmount(scale(rs.getBigDecimal("reward_amount")))
                .usedAt(rs.getTimestamp("used_at").toInstant())
                .build();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
