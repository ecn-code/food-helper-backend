package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "ProposedWeekMenuDayResponse", description = "API representation of one proposed day menu")
public record ProposedWeekMenuDayResponse(
        @Schema(description = "Proposed day identifier", example = "1")
        Long id,
        @Schema(description = "Date of the proposed day menu", example = "2026-06-15")
        LocalDate date,
        @Schema(description = "Configured day parts selected for the day")
        List<ProposedWeekMenuSectionResponse> sections,
        @Schema(description = "Nutritional totals for the day")
        NutritionalValuesResponse nutritionalValues
) {
}
