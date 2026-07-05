package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CaprichoPlanningCouponStrategy implements PlanningCouponStrategy {
    private static final BigDecimal REWARD_AMOUNT = new BigDecimal("10.00");
    private static final int PERIOD_DAYS = 90;

    @Override
    public String code() {
        return "CAPRICHO";
    }

    @Override
    public String name() {
        return "Capricho";
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
