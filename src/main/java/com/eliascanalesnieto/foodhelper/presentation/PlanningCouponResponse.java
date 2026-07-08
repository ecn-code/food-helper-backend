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
        @Schema(description = "Human-readable rule that the coupon requires", example = "The menu must fill every planned day with at least 3 products and cannot repeat the same product on the same day or in the same day part across different days")
        String conditionDescription,
        @Schema(description = "Whether the current evaluation context satisfies the coupon rule. Listing mode only checks cooldown, while validation and redemption also check the planning.", example = "true")
        boolean conditionMet,
        @Schema(description = "Money added to the user's money box when redeemed", example = "15.00")
        BigDecimal rewardAmount,
        @Schema(description = "Cooldown in days before the coupon can be used again", example = "30")
        int periodDays,
        @Schema(description = "Whether the coupon can be redeemed now", example = "true")
        boolean available,
        @Schema(description = "Whether the coupon was used recently and is still inside the cooldown window", example = "false")
        boolean usedRecently,
        @Schema(description = "Informative availability state considering conditions and cooldown", implementation = PlanningCouponAvailabilityState.class)
        PlanningCouponAvailabilityState informativeAvailabilityState,
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
