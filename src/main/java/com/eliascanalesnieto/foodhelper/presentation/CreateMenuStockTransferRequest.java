package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(name = "CreateMenuStockTransferRequest", description = "Transfer a stock entry from the global inventory into the temporary week stock of a menu")
public record CreateMenuStockTransferRequest(
        @NotNull
        @Schema(description = "Stock entry identifier to consume", example = "7")
        Long stockEntryId,
        @NotNull
        @DecimalMin(value = "0.01")
        @Schema(description = "Quantity to move from the stock entry into the menu stock", example = "1.50")
        BigDecimal quantity
) {
}
