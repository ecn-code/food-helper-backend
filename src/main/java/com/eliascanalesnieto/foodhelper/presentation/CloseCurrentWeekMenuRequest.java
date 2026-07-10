package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(name = "CloseMenuRequest", description = "People for whom the closed menu must be recorded")
public record CloseCurrentWeekMenuRequest(
        @NotEmpty
        @Schema(description = "Identifiers of the people who followed the menu", example = "[1, 2]")
        List<Long> personIds,
        @Schema(
                description = "Whether positive week stock should be transferred to the global stock when the menu is closed. When false, the stock is discarded from the menu snapshot.",
                example = "true",
                defaultValue = "true"
        )
        Boolean transferWeekStock
) {
    public CloseCurrentWeekMenuRequest(List<Long> personIds) {
        this(personIds, true);
    }

    public CloseCurrentWeekMenuRequest {
        transferWeekStock = transferWeekStock == null ? true : transferWeekStock;
    }
}
