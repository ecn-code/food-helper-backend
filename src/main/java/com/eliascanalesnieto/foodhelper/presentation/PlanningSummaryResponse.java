package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.PlanningState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "PlanningSummaryResponse", description = "Compact planning entry without days, products, or calculated totals")
public record PlanningSummaryResponse(
        @Schema(example = "42") Long id,
        @Schema(example = "2026-06-15") LocalDate startDate,
        @Schema(example = "2026-06-21") LocalDate endDate,
        @Schema(description = "Number of explicitly planned days", example = "5") int plannedDays,
        @Schema(allowableValues = {"DRAFT", "ESTABLISHED", "CLOSED"}, example = "ESTABLISHED") PlanningState state,
        @Schema(description = "Established menu identifier, or null while planning is a draft", example = "18", nullable = true) Long menuId
) {
}
