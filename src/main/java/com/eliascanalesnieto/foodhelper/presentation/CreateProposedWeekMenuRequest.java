package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(name = "CreatePlanningRequest", description = "Payload for starting empty planning. The inclusive date range cannot span more than 16 calendar days.")
public record CreateProposedWeekMenuRequest(
        @Schema(description = "First date covered by planning", example = "2026-06-15")
        @NotNull LocalDate startDate,
        @Schema(description = "Last date covered by planning", example = "2026-06-22")
        @NotNull LocalDate endDate
) {
}
