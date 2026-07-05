package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PlanningCouponAvailabilityState",
        description = "Informative availability state for a planning coupon"
)
public enum PlanningCouponAvailabilityState {
    AVAILABLE,
    CONDITION_NOT_MET,
    USED_RECENTLY,
    CONDITION_NOT_MET_AND_USED_RECENTLY
}
