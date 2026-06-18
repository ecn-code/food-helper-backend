package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "CurrentWeekMenuResponse", description = "API representation of an established week menu snapshot")
public record CurrentWeekMenuResponse(
        @Schema(description = "Established week menu identifier", example = "1")
        Long id,
        @Schema(description = "Proposed menu identifier used to create the established week", example = "5")
        Long proposedWeekMenuId,
        @Schema(description = "First date covered by the established week", example = "2026-06-15")
        LocalDate startDate,
        @Schema(description = "Last date covered by the established week", example = "2026-06-22")
        LocalDate endDate,
        @Schema(description = "Saved day menus in the established week")
        List<ProposedWeekMenuDayResponse> days,
        @Schema(description = "Nutritional totals for the whole established week")
        NutritionalValuesResponse nutritionalValues,
        @Schema(description = "Stock summary captured when the established week was created")
        ProposedWeekMenuStockSummaryResponse stockSummary,
        @Schema(description = "Stock entries consumed to satisfy the established week")
        List<CurrentWeekMenuUsedStockResponse> usedStock,
        @Schema(description = "Products still missing after consuming stock")
        List<CurrentWeekMenuShoppingListItemResponse> shoppingList
) {
}
