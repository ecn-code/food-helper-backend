package com.eliascanalesnieto.foodhelper.presentation;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "MenuResponse", description = "API representation of a menu created from planning")
public record CurrentWeekMenuResponse(
        @Schema(description = "Menu identifier", example = "1")
        Long id,
        @Schema(description = "Planning identifier used to create the menu", example = "5")
        @JsonAlias("proposedWeekMenuId") Long planningId,
        @Schema(description = "User identifier that assumes the menu cost", example = "1")
        Long payerUserId,
        @Schema(description = "Username that assumes the menu cost", example = "elias")
        String payerUsername,
        @Schema(description = "First date covered by the menu", example = "2026-06-15")
        LocalDate startDate,
        @Schema(description = "Last date covered by the menu", example = "2026-06-22")
        LocalDate endDate,
        @Schema(description = "Saved days in the menu")
        List<ProposedWeekMenuDayResponse> days,
        @Schema(description = "Nutritional totals for the whole menu")
        NutritionalValuesResponse nutritionalValues,
        @Schema(description = "Stock summary captured when the menu was created")
        ProposedWeekMenuStockSummaryResponse stockSummary,
        @Schema(description = "Stock entries consumed to satisfy the menu")
        List<CurrentWeekMenuUsedStockResponse> usedStock,
        @Schema(description = "Products still missing after consuming stock")
        List<CurrentWeekMenuShoppingListItemResponse> shoppingList,
        @Schema(description = "Repercussion movements recorded while the menu is open")
        List<MenuStockMovementResponse> stockMovements,
        @Schema(description = "Recipe productions scheduled for the menu with their stock transfer trace")
        List<CurrentWeekMenuRecipeProductionResponse> recipeProductions,
        @Schema(description = "Average daily nutrition evaluated against the saved rules")
        NutritionalRulesEvaluationResponse nutritionalRules
) {
}
