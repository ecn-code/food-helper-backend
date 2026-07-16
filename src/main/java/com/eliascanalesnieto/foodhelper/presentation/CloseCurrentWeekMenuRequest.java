package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
        Boolean transferWeekStock,
        @Schema(
                description = "Product identifiers whose positive week stock and pending recipe productions must not be transferred to global stock",
                example = "[10, 12]",
                defaultValue = "[]"
        )
        List<@NotNull @Positive Long> excludedPositiveStockProductIds
) {
    public CloseCurrentWeekMenuRequest(List<Long> personIds) {
        this(personIds, true, List.of());
    }

    public CloseCurrentWeekMenuRequest(List<Long> personIds, Boolean transferWeekStock) {
        this(personIds, transferWeekStock, List.of());
    }

    public CloseCurrentWeekMenuRequest {
        transferWeekStock = transferWeekStock == null ? true : transferWeekStock;
        excludedPositiveStockProductIds = excludedPositiveStockProductIds == null
                ? List.of()
                : List.copyOf(excludedPositiveStockProductIds);
    }
}
