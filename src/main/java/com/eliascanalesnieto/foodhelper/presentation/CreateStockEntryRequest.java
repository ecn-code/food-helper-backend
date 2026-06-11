package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "CreateStockEntryRequest", description = "Payload for creating a stock entry")
public record CreateStockEntryRequest(
        @Schema(description = "Available quantity for this stock entry", example = "6.5")
        @NotNull @Positive BigDecimal quantity,
        @Schema(description = "Stock expiration date when available", example = "2026-06-20", nullable = true)
        LocalDate expirationDate,
        @Schema(description = "Date when the stock entry was received", example = "2026-06-10")
        @NotNull LocalDate entryDate
) {
}
