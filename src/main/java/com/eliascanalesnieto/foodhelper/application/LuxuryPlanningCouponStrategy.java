package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class LuxuryPlanningCouponStrategy implements PlanningCouponStrategy {
    private static final BigDecimal REWARD_AMOUNT = new BigDecimal("50.00");
    private static final int PERIOD_DAYS = 180;

    @Override
    public String code() {
        return "LUXURY";
    }

    @Override
    public String name() {
        return "Luxury";
    }

    @Override
    public String conditionDescription() {
        return "No menu validation required";
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
        return true;
    }
}
