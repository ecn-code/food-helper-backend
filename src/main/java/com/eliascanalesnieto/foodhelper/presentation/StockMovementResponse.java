package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "StockMovementResponse", description = "API representation of a stock movement in the historical ledger")
public record StockMovementResponse(
        @Schema(description = "Historical movement identifier", example = "24")
        Long id,
        @Schema(description = "Linked product identifier", example = "1")
        Long productId,
        @Schema(description = "Linked product name at the time of the movement", example = "Apple")
        String productName,
        @Schema(description = "Related stock entry identifier when available", example = "3", nullable = true)
        Long stockEntryId,
        @Schema(description = "Movement type", example = "ADJUSTMENT")
        String movementType,
        @Schema(description = "Signed quantity change; positive values mean stock entered and negative values mean stock left", example = "-2.5")
        BigDecimal signedQuantity,
        @Schema(description = "Date when the movement became effective", example = "2026-06-10")
        LocalDate effectiveDate,
        @Schema(description = "Timestamp when the movement was recorded", example = "2026-06-10T14:30:00")
        LocalDateTime recordedAt,
        @Schema(description = "Price per unit at the time of the movement", example = "4.99", nullable = true)
        BigDecimal price,
        @Schema(description = "Expiration date captured with the movement when available", example = "2026-06-20", nullable = true)
        LocalDate expirationDate,
        @Schema(description = "Entry date captured with the movement when available", example = "2026-06-10", nullable = true)
        LocalDate entryDate
) {
}
