package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Schema(name = "CreateMenuRequest", description = "Request used to create a menu from planning and assign its cost")
public record EstablishProposedWeekMenuRequest(
        @NotNull
        @Schema(description = "User identifier that assumes the menu cost", example = "1")
        Long payerUserId,
        @Valid
        @Schema(description = "Optional user-confirmed stock allocation. When omitted or empty, stock is allocated automatically by earliest expiration date.")
        List<MenuStockAllocationRequest> stockAllocations,
        @Schema(description = "Optional stable coupon codes to redeem while the menu is being established.", example = "[\"NO_REPEATED_PRODUCTS\"]")
        List<String> couponCodes
) {
    public EstablishProposedWeekMenuRequest(Long payerUserId) {
        this(payerUserId, null, null);
    }

    public EstablishProposedWeekMenuRequest(Long payerUserId, List<MenuStockAllocationRequest> stockAllocations) {
        this(payerUserId, stockAllocations, null);
    }
}
