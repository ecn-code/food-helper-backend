package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ProductStatsResponse", description = "Aggregated product and stock statistics calculated on the server")
public record ProductStatsResponse(
        @Schema(description = "Product with the highest calories per 100 grams")
        ProductStatsMetricResponse caloriesTop,
        @Schema(description = "Product with the highest carbohydrates per 100 grams")
        ProductStatsMetricResponse carbohydratesTop,
        @Schema(description = "Product with the highest proteins per 100 grams")
        ProductStatsMetricResponse proteinsTop,
        @Schema(description = "Product with the highest fats per 100 grams")
        ProductStatsMetricResponse fatsTop,
        @Schema(description = "Aggregated stock totals")
        ProductStatsTotalsResponse stock,
        @Schema(description = "Product whose lot expires first")
        ProductStatsExpirationResponse earliestExpiration,
        @Schema(description = "Per-product stock summary ready for charts and inventory cards")
        List<ProductStatsSummaryResponse> summaries
) {
}
