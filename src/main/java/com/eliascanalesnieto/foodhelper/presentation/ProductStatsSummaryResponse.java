package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "ProductStatsSummaryResponse", description = "Aggregated stock summary for one product")
public record ProductStatsSummaryResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Chicken breast")
        String productName,
        @Schema(description = "Total quantity aggregated across all stock entries for this product", example = "12.50")
        BigDecimal totalQuantity,
        @Schema(description = "Number of stock entries for this product", example = "2")
        long batchCount,
        @Schema(description = "Earliest expiration date among this product's stock entries", example = "2026-06-20", nullable = true)
        LocalDate nextExpirationDate,
        @Schema(description = "Fallback label when the product has no stock or no expiration dates", example = "Sin caducidad", nullable = true)
        String nextExpirationMessage
) {
}
