package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class PlanningCouponRedemption {
    Long id;
    Long userId;
    String couponCode;
    Long planningId;
    Long currentWeekMenuId;
    BigDecimal rewardAmount;
    Instant usedAt;
}
