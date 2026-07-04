package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemption;
import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemptionRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
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
public class JdbcPlanningCouponRedemptionRepository implements PlanningCouponRedemptionRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<PlanningCouponRedemption> findLatestByUserIdAndCouponCode(Long userId, String couponCode) {
        List<PlanningCouponRedemption> redemptions = jdbcTemplate.query("""
                        SELECT id, user_id, coupon_code, planning_id, current_week_menu_id, reward_amount, used_at
                        FROM planning_coupon_redemptions
                        WHERE user_id = ? AND coupon_code = ?
                        ORDER BY used_at DESC, id DESC
                        LIMIT 1
                        """,
                redemptionRowMapper(),
                userId,
                couponCode
        );
        return redemptions.stream().findFirst();
    }

    @Override
    public PlanningCouponRedemption save(PlanningCouponRedemption redemption) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO planning_coupon_redemptions (user_id, coupon_code, planning_id, current_week_menu_id, reward_amount, used_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, redemption.getUserId());
            ps.setString(2, redemption.getCouponCode());
            ps.setLong(3, redemption.getPlanningId());
            ps.setLong(4, redemption.getCurrentWeekMenuId());
            ps.setBigDecimal(5, scale(redemption.getRewardAmount()));
            ps.setTimestamp(6, java.sql.Timestamp.from(redemption.getUsedAt()));
            return ps;
        }, keyHolder);
        return redemption.toBuilder()
                .id(keyHolder.getKey().longValue())
                .build();
    }

    @Override
    public void deleteByCurrentWeekMenuId(Long currentWeekMenuId) {
        jdbcTemplate.update("DELETE FROM planning_coupon_redemptions WHERE current_week_menu_id = ?", currentWeekMenuId);
    }

    private RowMapper<PlanningCouponRedemption> redemptionRowMapper() {
        return (rs, rowNum) -> PlanningCouponRedemption.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .couponCode(rs.getString("coupon_code"))
                .planningId(rs.getLong("planning_id"))
                .currentWeekMenuId(rs.getLong("current_week_menu_id"))
                .rewardAmount(scale(rs.getBigDecimal("reward_amount")))
                .usedAt(rs.getTimestamp("used_at").toInstant())
                .build();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
