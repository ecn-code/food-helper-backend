package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(name = "CreatePlanningRequest", description = "Payload for starting empty planning. The inclusive date range cannot span more than 16 calendar days, and the user count defaults to 1 when omitted.")
public record CreateProposedWeekMenuRequest(
        @Schema(description = "First date covered by planning", example = "2026-06-15")
        @NotNull LocalDate startDate,
        @Schema(description = "Last date covered by planning", example = "2026-06-22")
        @NotNull LocalDate endDate,
        @Schema(description = "Number of users covered by the planning", example = "4", defaultValue = "1")
        @Min(1) Integer users
) {
    public CreateProposedWeekMenuRequest {
        users = users == null ? 1 : users;
    }

    public CreateProposedWeekMenuRequest(LocalDate startDate, LocalDate endDate) {
        this(startDate, endDate, 1);
    }
}
