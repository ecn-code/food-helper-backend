package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(name = "PlanningCouponResponse", description = "Coupon availability and redemption details for one planning")
public record PlanningCouponResponse(
        @Schema(description = "Stable coupon code", example = "NO_REPEATED_PRODUCTS")
        String code,
        @Schema(description = "Coupon display name", example = "No repeated products")
        String name,
        @Schema(description = "Money added to the user's money box when redeemed", example = "20.00")
        BigDecimal rewardAmount,
        @Schema(description = "Cooldown in days before the coupon can be used again", example = "30")
        int periodDays,
        @Schema(description = "Whether the coupon can be redeemed now", example = "true")
        boolean available,
        @Schema(description = "Instant when the coupon was last redeemed, or null if never used", nullable = true, example = "2026-06-01T10:15:30Z")
        Instant lastUsedAt,
        @Schema(description = "Instant when the coupon becomes available again after cooldown, or null when it is blocked only by the planning conditions", nullable = true, example = "2026-07-01T10:15:30Z")
        Instant nextAvailableAt,
        @ArraySchema(schema = @Schema(implementation = PlanningCouponUnavailabilityReason.class),
                arraySchema = @Schema(description = "Reasons why the coupon cannot be redeemed now", nullable = true))
        List<PlanningCouponUnavailabilityReason> unavailableReasons
) {
    public PlanningCouponResponse {
        unavailableReasons = unavailableReasons == null ? List.of() : List.copyOf(unavailableReasons);
    }
}
