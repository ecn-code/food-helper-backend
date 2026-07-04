package com.eliascanalesnieto.foodhelper.domain;

import java.util.Optional;

public interface PlanningCouponRedemptionRepository {
    Optional<PlanningCouponRedemption> findLatestByUserIdAndCouponCode(Long userId, String couponCode);

    PlanningCouponRedemption save(PlanningCouponRedemption redemption);

    void deleteByCurrentWeekMenuId(Long currentWeekMenuId);
}
