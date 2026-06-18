package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "ProposedWeekMenuDayPartRequest", description = "Payload for creating or updating a reusable day part configuration")
public record ProposedWeekMenuDayPartRequest(
        @Schema(description = "Day part name", example = "Lunch")
        @NotBlank String name,
        @Schema(description = "Day part description", example = "Main meal of the day")
        @NotBlank String description,
        @Schema(description = "Explicit display order among day parts", example = "20")
        @NotNull @PositiveOrZero Integer sortOrder
) {
}
