package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "CouponResponse", description = "Global coupon catalog entry with availability details")
public record CouponResponse(
        @Schema(description = "Coupon definition identifier", example = "1") Long id,
        @Schema(description = "Stable coupon code", example = "NO_REPEATED_PRODUCTS")
        String code,
        @Schema(description = "Coupon display name", example = "No repeated products")
        String name,
        @Schema(description = "Human-readable rule that the coupon requires", example = "The menu must fill every planned day with at least 3 products and cannot repeat the same product on the same day or in the same day part across different days")
        String conditionDescription,
        @Schema(description = "Eligibility rule code", example = "ALWAYS") String ruleCode,
        @Schema(description = "Money added when redeemed", example = "10.00") java.math.BigDecimal rewardAmount,
        @Schema(description = "Cooldown in days", example = "30") int periodDays,
        @Schema(description = "Whether the coupon can be redeemed now", example = "true")
        boolean available,
        @ArraySchema(
                schema = @Schema(implementation = PlanningCouponUnavailabilityReason.class),
                arraySchema = @Schema(description = "Reasons why the coupon cannot be redeemed now", nullable = true)
        )
        List<PlanningCouponUnavailabilityReason> unavailableReasons
) {
    public CouponResponse {
        unavailableReasons = unavailableReasons == null ? List.of() : List.copyOf(unavailableReasons);
    }
}
