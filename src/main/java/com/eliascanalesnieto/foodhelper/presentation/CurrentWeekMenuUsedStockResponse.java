package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "CurrentWeekMenuUsedStockResponse", description = "Stock entry consumption record for an established week menu")
public record CurrentWeekMenuUsedStockResponse(
        @Schema(description = "Stock entry identifier", example = "7")
        Long stockEntryId,
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Apple")
        String productName,
        @Schema(description = "Quantity consumed from the stock entry", example = "1.50")
        BigDecimal usedUnits,
        @Schema(description = "Price paid for one unit in this stock entry", example = "2.49")
        BigDecimal price,
        @Schema(description = "Total cost of the consumed quantity", example = "3.74")
        BigDecimal totalCost,
        @Schema(description = "Stock expiration date when available", example = "2026-06-20", nullable = true)
        LocalDate expirationDate,
        @Schema(description = "Date when the stock entry was received", example = "2026-06-10")
        LocalDate entryDate
) {
}
