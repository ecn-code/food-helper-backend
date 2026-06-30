package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "UpsertPlannedDayRequest", description = "Payload for creating or replacing one day inside a planning")
public record UpsertProposedWeekMenuDayRequest(
        @Schema(description = "Date of the planned day", example = "2026-06-15")
        @NotNull LocalDate date,
        @Schema(description = "Configured day parts selected for the day. Each day part can appear only once.")
        List<@Valid ProposedWeekMenuSectionRequest> sections,
        @Schema(description = "Recipe productions scheduled for the day. Recipes do not count as meals and instead generate stock.")
        List<@Valid ProposedWeekMenuRecipeProductionRequest> recipeProductions
) {
    public UpsertProposedWeekMenuDayRequest(LocalDate date, List<@Valid ProposedWeekMenuSectionRequest> sections) {
        this(date, sections, null);
    }
}
