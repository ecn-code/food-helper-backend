package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Schema(name = "CreateMenuRequest", description = "Request used to create a menu from planning, assign its cost, and optionally attach the people assigned to it. When person identifiers are provided, the execution recalculates the stock for that group size.")
public record EstablishProposedWeekMenuRequest(
        @NotNull
        @Schema(description = "User identifier that assumes the menu cost", example = "1")
        Long payerUserId,
        @Valid
        @Schema(description = "Optional user-confirmed stock allocation. When omitted or empty, stock is allocated automatically by earliest expiration date.")
        List<MenuStockAllocationRequest> stockAllocations,
        @Schema(description = "Optional stable coupon codes to redeem while the menu is being established.", example = "[\"NO_REPEATED_PRODUCTS\"]")
        List<String> couponCodes,
        @Schema(description = "Optional identifiers of the people assigned to the menu when it is established. Their count is used to recalculate the stock requirement for the execution.", example = "[1, 7]")
        List<Long> personIds
) {
    public EstablishProposedWeekMenuRequest(Long payerUserId) {
        this(payerUserId, null, null, null);
    }

    public EstablishProposedWeekMenuRequest(Long payerUserId, List<MenuStockAllocationRequest> stockAllocations) {
        this(payerUserId, stockAllocations, null, null);
    }

    public EstablishProposedWeekMenuRequest(
            Long payerUserId,
            List<MenuStockAllocationRequest> stockAllocations,
            List<String> couponCodes
    ) {
        this(payerUserId, stockAllocations, couponCodes, null);
    }
}
