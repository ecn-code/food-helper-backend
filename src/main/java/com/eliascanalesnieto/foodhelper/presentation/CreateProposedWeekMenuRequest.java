package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(name = "CreateProposedWeekMenuRequest", description = "Payload for starting an empty proposed week menu")
public record CreateProposedWeekMenuRequest(
        @Schema(description = "First date covered by the proposed menu", example = "2026-06-15")
        @NotNull LocalDate startDate,
        @Schema(description = "Last date covered by the proposed menu", example = "2026-06-21")
        @NotNull LocalDate endDate
) {
}
