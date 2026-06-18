package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ProductStatsTotalsResponse", description = "Aggregated stock totals")
public record ProductStatsTotalsResponse(
        @Schema(description = "Total quantity across all stock entries", example = "18.50")
        BigDecimal totalQuantity,
        @Schema(description = "Number of stock entries", example = "3")
        long batchCount
) {
}
