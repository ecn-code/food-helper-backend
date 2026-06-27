package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PlanningDayPartResponse", description = "API representation of one reusable day part configuration")
public record ProposedWeekMenuDayPartResponse(
        @Schema(description = "Day part identifier", example = "1")
        Long id,
        @Schema(description = "Day part name", example = "Lunch")
        String name,
        @Schema(description = "Day part description", example = "Main meal of the day")
        String description,
        @Schema(description = "Explicit display order among day parts", example = "20")
        Integer sortOrder
) {
}
