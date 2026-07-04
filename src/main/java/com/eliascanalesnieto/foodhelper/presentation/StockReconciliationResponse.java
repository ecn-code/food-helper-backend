package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "StockReconciliationResponse", description = "Summary used to reconcile historical stock movements with the live stock")
public record StockReconciliationResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Apple")
        String productName,
        @Schema(description = "Historical quantity calculated from all ledger movements", example = "10.00")
        BigDecimal calculatedQuantity,
        @Schema(description = "Current live quantity stored in stock entries", example = "11.00")
        BigDecimal liveQuantity,
        @Schema(description = "Difference between the live and calculated quantities", example = "1.00")
        BigDecimal difference,
        @Schema(description = "Total positive quantity recorded in the history", example = "100.00")
        BigDecimal totalIn,
        @Schema(description = "Total negative quantity recorded in the history", example = "90.00")
        BigDecimal totalOut,
        @Schema(description = "Total number of movements registered for the product", example = "12")
        long movementCount
) {
}
