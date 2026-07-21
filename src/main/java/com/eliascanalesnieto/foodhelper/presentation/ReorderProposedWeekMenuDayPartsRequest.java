package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(name = "ReorderPlanningDayPartsRequest", description = "Exact ordered set of planning day-part identifiers")
public record ReorderProposedWeekMenuDayPartsRequest(
        @NotEmpty
        @Schema(description = "Every current day-part identifier, once, in its desired order", example = "[3, 1, 2]")
        List<@NotNull @Positive Long> dayPartIds
) {
}
