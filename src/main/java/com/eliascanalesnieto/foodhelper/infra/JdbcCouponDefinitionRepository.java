package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.CouponDefinition;
import com.eliascanalesnieto.foodhelper.domain.CouponDefinitionRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCouponDefinitionRepository implements CouponDefinitionRepository {
    private final JdbcClient jdbcClient;

    @Override public List<CouponDefinition> findAll() {
        return jdbcClient.sql("SELECT id, code, name, condition_description, rule_code, reward_amount, period_days FROM coupon_definitions ORDER BY code")
                .query(this::map).list();
    }
    @Override public CouponDefinition findByCode(String code) {
        return jdbcClient.sql("SELECT id, code, name, condition_description, rule_code, reward_amount, period_days FROM coupon_definitions WHERE code = :code")
                .param("code", code).query(this::map).optional().orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }
    @Override public CouponDefinition create(CouponDefinition coupon) {
        try {
            Long id = jdbcClient.sql("INSERT INTO coupon_definitions (code, name, condition_description, rule_code, reward_amount, period_days) VALUES (:code, :name, :description, :ruleCode, :rewardAmount, :periodDays) RETURNING id")
                    .param("code", coupon.getCode()).param("name", coupon.getName()).param("description", coupon.getConditionDescription()).param("ruleCode", coupon.getRuleCode()).param("rewardAmount", coupon.getRewardAmount()).param("periodDays", coupon.getPeriodDays()).query(Long.class).single();
            return coupon.toBuilder().id(id).build();
        } catch (DataIntegrityViolationException ex) { throw new DuplicateResourceException("Coupon code already exists"); }
    }
    @Override public CouponDefinition update(String code, CouponDefinition coupon) {
        try {
            int count = jdbcClient.sql("UPDATE coupon_definitions SET name = :name, condition_description = :description, rule_code = :ruleCode, reward_amount = :rewardAmount, period_days = :periodDays WHERE code = :code")
                    .param("code", code).param("name", coupon.getName()).param("description", coupon.getConditionDescription()).param("ruleCode", coupon.getRuleCode()).param("rewardAmount", coupon.getRewardAmount()).param("periodDays", coupon.getPeriodDays()).update();
            if (count == 0) throw new ResourceNotFoundException("Coupon not found");
            return findByCode(code);
        } catch (DataIntegrityViolationException ex) { throw new DuplicateResourceException("Coupon code already exists"); }
    }
    @Override public void delete(String code) {
        if (jdbcClient.sql("DELETE FROM coupon_definitions WHERE code = :code").param("code", code).update() == 0) throw new ResourceNotFoundException("Coupon not found");
    }
    private CouponDefinition map(java.sql.ResultSet rs, int row) throws java.sql.SQLException { return CouponDefinition.builder().id(rs.getLong("id")).code(rs.getString("code")).name(rs.getString("name")).conditionDescription(rs.getString("condition_description")).ruleCode(rs.getString("rule_code")).rewardAmount(rs.getBigDecimal("reward_amount")).periodDays(rs.getInt("period_days")).build(); }
}
