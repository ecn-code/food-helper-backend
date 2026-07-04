package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PlanningCouponUnavailabilityReason",
        description = "Reason why a coupon cannot be redeemed at the moment",
        enumAsRef = true
)
public enum PlanningCouponUnavailabilityReason {
    CONDITION_NOT_MET,
    USED_WITHIN_PERIOD
}
