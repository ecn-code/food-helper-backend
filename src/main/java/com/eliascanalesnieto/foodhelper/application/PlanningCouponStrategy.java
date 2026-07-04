package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemption;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import java.time.Instant;

public interface PlanningCouponStrategy {
    String code();

    String name();

    int periodDays();

    java.math.BigDecimal rewardAmount();

    boolean matches(ProposedWeekMenu proposedMenu);

    default PlanningCouponResponse evaluate(
            ProposedWeekMenu proposedMenu,
            PlanningCouponRedemption lastRedemption,
            Instant now
    ) {
        java.util.List<com.eliascanalesnieto.foodhelper.presentation.PlanningCouponUnavailabilityReason> reasons =
                new java.util.ArrayList<>();
        if (!matches(proposedMenu)) {
            reasons.add(com.eliascanalesnieto.foodhelper.presentation.PlanningCouponUnavailabilityReason.CONDITION_NOT_MET);
        }
        Instant lastUsedAt = lastRedemption == null ? null : lastRedemption.getUsedAt();
        Instant nextAvailableAt = null;
        if (lastUsedAt != null) {
            nextAvailableAt = lastUsedAt.plus(periodDays(), java.time.temporal.ChronoUnit.DAYS);
            if (nextAvailableAt.isAfter(now)) {
                reasons.add(com.eliascanalesnieto.foodhelper.presentation.PlanningCouponUnavailabilityReason.USED_WITHIN_PERIOD);
            } else {
                nextAvailableAt = null;
            }
        }
        return new PlanningCouponResponse(
                code(),
                name(),
                rewardAmount(),
                periodDays(),
                reasons.isEmpty(),
                lastUsedAt,
                nextAvailableAt,
                reasons
        );
    }
}
