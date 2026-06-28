package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(name = "MenuStockAllocationRequest", description = "Explicit quantity to consume from one stock entry")
public record MenuStockAllocationRequest(
        @NotNull
        @Schema(description = "Stock entry identifier", example = "7")
        Long stockEntryId,
        @NotNull
        @DecimalMin(value = "0.01")
        @Schema(description = "Quantity to consume from this stock entry", example = "1.50")
        BigDecimal usedUnits
) {
}
