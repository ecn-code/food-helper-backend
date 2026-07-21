package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "PlanningDayResponse", description = "API representation of one planned day")
public record ProposedWeekMenuDayResponse(
        @Schema(description = "Planned day identifier", example = "1")
        Long id,
        @Schema(description = "Date of the planned day", example = "2026-06-15")
        LocalDate date,
        @Schema(description = "Configured day parts selected for the day")
        List<ProposedWeekMenuSectionResponse> sections,
        @Schema(description = "Recipe productions scheduled for the day")
        List<ProposedWeekMenuRecipeProductionResponse> recipeProductions,
        @Schema(description = "Nutritional totals for the day")
        NutritionalValuesResponse nutritionalValues,
        @Schema(description = "Evaluation of this day's nutrition against the configured daily rules")
        NutritionalRulesEvaluationPeriodResponse dailyNutritionalEvaluation
) {
    /** Compatibility constructor for persisted snapshots and existing clients. */
    public ProposedWeekMenuDayResponse(Long id, LocalDate date, List<ProposedWeekMenuSectionResponse> sections,
            List<ProposedWeekMenuRecipeProductionResponse> recipeProductions, NutritionalValuesResponse nutritionalValues) {
        this(id, date, sections, recipeProductions, nutritionalValues, null);
    }
}
