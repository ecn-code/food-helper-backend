package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class SushiPlanningCouponStrategy implements PlanningCouponStrategy {
    private static final BigDecimal REWARD_AMOUNT = new BigDecimal("20.00");
    private static final int PERIOD_DAYS = 60;
    private static final Long SUSHI_PRODUCT_ID = 256L;

    @Override
    public String code() {
        return "SUSHI";
    }

    @Override
    public String name() {
        return "Sushi";
    }

    @Override
    public String conditionDescription() {
        return "The menu must include product 256";
    }

    @Override
    public int periodDays() {
        return PERIOD_DAYS;
    }

    @Override
    public BigDecimal rewardAmount() {
        return REWARD_AMOUNT;
    }

    @Override
    public boolean matches(ProposedWeekMenu proposedMenu) {
        return PlanningCouponMenuInspector.productIdsFrom(proposedMenu).contains(SUSHI_PRODUCT_ID);
    }
}
