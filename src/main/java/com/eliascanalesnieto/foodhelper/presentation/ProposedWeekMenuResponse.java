package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "ProposedWeekMenuResponse", description = "API representation of a proposed week menu")
public record ProposedWeekMenuResponse(
        @Schema(description = "Proposed menu identifier", example = "1")
        Long id,
        @Schema(description = "First date covered by the proposed menu", example = "2026-06-15")
        LocalDate startDate,
        @Schema(description = "Last date covered by the proposed menu", example = "2026-06-21")
        LocalDate endDate,
        @Schema(description = "Existing proposed day menus. A proposed week menu may start empty.")
        List<ProposedWeekMenuDayResponse> days,
        @Schema(description = "Nutritional totals for the whole proposed menu")
        NutritionalValuesResponse nutritionalValues
) {
}
