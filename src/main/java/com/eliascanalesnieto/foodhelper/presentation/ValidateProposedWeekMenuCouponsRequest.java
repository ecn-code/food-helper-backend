package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(name = "ValidatePlanningCouponsRequest", description = "Request used to validate selected coupons against an existing planning")
public record ValidateProposedWeekMenuCouponsRequest(
        @NotNull
        @Schema(description = "User identifier that wants to validate the coupons", example = "1")
        Long payerUserId,
        @Valid
        @Schema(description = "Stable coupon codes to validate against the planning", example = "[\"NO_REPEATED_PRODUCTS\"]")
        List<String> couponCodes
) {
}
