package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "StockEntryResponse", description = "API representation of a stock entry")
public record StockEntryResponse(
        @Schema(description = "Stock entry identifier", example = "3")
        Long id,
        @Schema(description = "Linked product identifier", example = "1")
        Long productId,
        @Schema(description = "Linked product name", example = "Apple")
        String productName,
        @Schema(description = "Available quantity in this stock entry, returned in units when isStockInUnits is true", example = "6.5")
        BigDecimal quantity,
        @Schema(description = "Whether quantity is returned in units rather than stored grams", example = "false")
        boolean isStockInUnits,
        @Schema(description = "Price paid for this stock entry. Supports up to four decimal places", example = "0.0068")
        BigDecimal price,
        @Schema(description = "Stock expiration date when available", example = "2026-06-20", nullable = true)
        LocalDate expirationDate,
        @Schema(description = "Date when the stock entry was received", example = "2026-06-10")
        LocalDate entryDate
) {
}
