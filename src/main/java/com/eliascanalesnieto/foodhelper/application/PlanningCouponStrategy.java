package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemption;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponAvailabilityState;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponUnavailabilityReason;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public interface PlanningCouponStrategy {
    String code();

    String name();

    String conditionDescription();

    int periodDays();

    java.math.BigDecimal rewardAmount();

    boolean matches(ProposedWeekMenu proposedMenu);

    default boolean matches(ProposedWeekMenu proposedMenu, Long payerUserId, Instant now) {
        return matches(proposedMenu);
    }

    default PlanningCouponResponse evaluateTemporal(PlanningCouponRedemption lastRedemption, Instant now) {
        Instant lastUsedAt = lastRedemption == null ? null : lastRedemption.getUsedAt();
        Instant nextAvailableAt = null;
        boolean usedRecently = false;
        if (lastUsedAt != null) {
            nextAvailableAt = lastUsedAt.plus(periodDays(), java.time.temporal.ChronoUnit.DAYS);
            if (nextAvailableAt.isAfter(now)) {
                usedRecently = true;
            } else {
                nextAvailableAt = null;
            }
        }
        boolean available = !usedRecently;
        return new PlanningCouponResponse(
                code(),
                name(),
                conditionDescription(),
                true,
                rewardAmount(),
                periodDays(),
                available,
                usedRecently,
                informativeAvailabilityState(true, usedRecently),
                lastUsedAt,
                nextAvailableAt,
                usedRecently
                        ? List.of(PlanningCouponUnavailabilityReason.USED_WITHIN_PERIOD)
                        : List.of()
        );
    }

    default PlanningCouponResponse evaluate(
            ProposedWeekMenu proposedMenu,
            Long payerUserId,
            PlanningCouponRedemption lastRedemption,
            Instant now
    ) {
        List<PlanningCouponUnavailabilityReason> reasons = new ArrayList<>();
        boolean conditionMet = matches(proposedMenu, payerUserId, now);
        if (!conditionMet) {
            reasons.add(PlanningCouponUnavailabilityReason.CONDITION_NOT_MET);
        }
        Instant lastUsedAt = lastRedemption == null ? null : lastRedemption.getUsedAt();
        Instant nextAvailableAt = null;
        boolean usedRecently = false;
        if (lastUsedAt != null) {
            nextAvailableAt = lastUsedAt.plus(periodDays(), java.time.temporal.ChronoUnit.DAYS);
            if (nextAvailableAt.isAfter(now)) {
                reasons.add(PlanningCouponUnavailabilityReason.USED_WITHIN_PERIOD);
                usedRecently = true;
            } else {
                nextAvailableAt = null;
            }
        }
        boolean available = reasons.isEmpty();
        return new PlanningCouponResponse(
                code(),
                name(),
                conditionDescription(),
                conditionMet,
                rewardAmount(),
                periodDays(),
                available,
                usedRecently,
                informativeAvailabilityState(conditionMet, usedRecently),
                lastUsedAt,
                nextAvailableAt,
                reasons
        );
    }

    default PlanningCouponAvailabilityState informativeAvailabilityState(boolean conditionMet, boolean usedRecently) {
        if (conditionMet && !usedRecently) {
            return PlanningCouponAvailabilityState.AVAILABLE;
        }
        if (!conditionMet && usedRecently) {
            return PlanningCouponAvailabilityState.CONDITION_NOT_MET_AND_USED_RECENTLY;
        }
        if (!conditionMet) {
            return PlanningCouponAvailabilityState.CONDITION_NOT_MET;
        }
        return PlanningCouponAvailabilityState.USED_RECENTLY;
    }
}
