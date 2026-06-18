package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ProductStatsMetricResponse", description = "Top product for a nutritional metric")
public record ProductStatsMetricResponse(
        @Schema(description = "Product identifier when the catalog has data", example = "1", nullable = true)
        Long productId,
        @Schema(description = "Product name or fallback label when there is no data", example = "Chicken breast")
        String productName,
        @Schema(description = "Metric value", example = "165.00")
        BigDecimal value,
        @Schema(description = "Fallback label for empty catalogs", example = "Sin datos", nullable = true)
        String message
) {
}
