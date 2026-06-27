package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "PlanningStockSummaryResponse", description = "Preview of stock needs and nutritional coverage for the planning")
public record ProposedWeekMenuStockSummaryResponse(
        @Schema(description = "Days in the menu that already have at least one saved day", example = "3")
        Integer plannedDays,
        @Schema(description = "Distinct products used across all planned days", example = "8")
        Integer distinctProducts,
        @Schema(description = "Calorie summary across planned days")
        ProposedWeekMenuStockSummaryCaloriesResponse calories,
        @Schema(description = "Total estimated cost covered by available stock", example = "18.50")
        BigDecimal estimatedCost,
        @Schema(description = "Per-product stock coverage requirements")
        List<ProposedWeekMenuStockRequirementResponse> requirements
) {
}
