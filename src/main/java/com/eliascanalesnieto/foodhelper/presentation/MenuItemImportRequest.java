package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "MenuItemImportRequest", description = "One purchased item to import into a selected destination")
public record MenuItemImportRequest(
        @NotNull
        @Positive
        @Schema(description = "Purchased product identifier", example = "12")
        Long productId,
        @NotNull
        @DecimalMin(value = "0.01")
        @Schema(description = "Purchased quantity", example = "3")
        BigDecimal quantity,
        @NotNull
        @DecimalMin(value = "0.00")
        @Schema(description = "Purchased unit price", example = "2.49")
        BigDecimal price,
        @NotNull
        @Schema(description = "Destination that receives this row")
        MenuItemImportDestination destination,
        @Schema(description = "Required only for MONEY_BOX and rejected for other destinations", example = "4", nullable = true)
        Long moneyBoxId,
        @Schema(description = "Optional ISO-8601 expiration date accepted only for GLOBAL_STOCK", example = "2026-08-31", nullable = true)
        LocalDate expirationDate
) {
}
