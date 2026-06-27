package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "PlanningResponse", description = "API representation of menu planning for a flexible date range")
public record ProposedWeekMenuResponse(
        @Schema(description = "Planning identifier", example = "1")
        Long id,
        @Schema(description = "First date covered by planning", example = "2026-06-15")
        LocalDate startDate,
        @Schema(description = "Last date covered by planning", example = "2026-06-22")
        LocalDate endDate,
        @Schema(description = "Existing planned days. Planning may start empty.")
        List<ProposedWeekMenuDayResponse> days,
        @Schema(description = "Nutritional totals for all planned days")
        NutritionalValuesResponse nutritionalValues,
        @Schema(description = "Preview of stock requirements and calorie coverage for the planning period")
        ProposedWeekMenuStockSummaryResponse stockSummary,
        @Schema(description = "Average daily nutrition evaluated against the saved rules")
        NutritionalRulesEvaluationResponse nutritionalRules
) {
}
