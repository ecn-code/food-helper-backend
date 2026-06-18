package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "ProductStatsExpirationResponse", description = "Product that expires first across all stock entries")
public record ProductStatsExpirationResponse(
        @Schema(description = "Product identifier when an expiring lot exists", example = "1", nullable = true)
        Long productId,
        @Schema(description = "Product name when an expiring lot exists", example = "Chicken breast")
        String productName,
        @Schema(description = "Quantity of the earliest expiring stock entry", example = "2.00", nullable = true)
        BigDecimal quantity,
        @Schema(description = "Earliest expiration date across all stock entries", example = "2026-06-20", nullable = true)
        LocalDate expirationDate,
        @Schema(description = "Fallback label when there are no lots or no expiring lots", example = "Sin caducidad", nullable = true)
        String message
) {
}
