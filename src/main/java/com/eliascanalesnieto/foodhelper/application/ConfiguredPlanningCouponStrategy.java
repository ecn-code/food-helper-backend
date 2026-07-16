package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.CouponDefinition;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import java.time.Instant;

final class ConfiguredPlanningCouponStrategy implements PlanningCouponStrategy {
    private final CouponDefinition definition;
    private final PlanningCouponStrategy rule;

    ConfiguredPlanningCouponStrategy(CouponDefinition definition, PlanningCouponStrategy rule) {
        this.definition = definition;
        this.rule = rule;
    }
    public String code() { return definition.getCode(); }
    public String name() { return definition.getName(); }
    public String conditionDescription() { return definition.getConditionDescription(); }
    public int periodDays() { return definition.getPeriodDays(); }
    public java.math.BigDecimal rewardAmount() { return definition.getRewardAmount(); }
    public boolean matches(ProposedWeekMenu menu) { return rule == null || rule.matches(menu); }
    public boolean matches(ProposedWeekMenu menu, Long payerUserId, Instant now) { return rule == null || rule.matches(menu, payerUserId, now); }
}
