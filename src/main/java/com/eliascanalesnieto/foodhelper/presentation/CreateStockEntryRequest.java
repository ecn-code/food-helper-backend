package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "CreateStockEntryRequest", description = "Payload for creating a stock entry")
public record CreateStockEntryRequest(
        @Schema(description = "Available quantity for this stock entry, always supplied and stored in grams", example = "150")
        @NotNull @Positive BigDecimal quantity,
        @Schema(description = "Price paid for this stock entry. Supports up to four decimal places", example = "0.0068")
        @NotNull @PositiveOrZero BigDecimal price,
        @Schema(description = "Stock expiration date when available", example = "2026-06-20", nullable = true)
        LocalDate expirationDate,
        @Schema(description = "Date when the stock entry was received", example = "2026-06-10")
        @NotNull LocalDate entryDate
) {
}
