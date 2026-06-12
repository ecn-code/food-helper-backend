package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "UpsertProposedWeekMenuDayRequest", description = "Payload for creating or replacing one day inside a proposed week menu")
public record UpsertProposedWeekMenuDayRequest(
        @Schema(description = "Date of the proposed day menu", example = "2026-06-15")
        @NotNull LocalDate date,
        @Schema(description = "Ordered sections for the day, such as lunch or snack")
        @NotEmpty List<@Valid ProposedWeekMenuSectionRequest> sections
) {
}
