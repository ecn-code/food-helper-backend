package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "PlanningRecipeProductionResponse", description = "Recipe production planned for one day and the product stock it will generate")
public record ProposedWeekMenuRecipeProductionResponse(
        @Schema(description = "Recipe production identifier", example = "1")
        Long id,
        @Schema(description = "Recipe identifier", example = "12")
        Long recipeId,
        @Schema(description = "Recipe name", example = "Chicken curry")
        String recipeName,
        @Schema(description = "Product identifier created from the recipe", example = "22")
        Long productId,
        @Schema(description = "Product name created from the recipe", example = "Chicken curry")
        String productName,
        @Schema(description = "Produced grams that will be transferred to stock", example = "400.00")
        BigDecimal producedGrams,
        @Schema(description = "Produced units derived from the linked product grams per unit", example = "4.00")
        BigDecimal producedUnits,
        @Schema(description = "Explicit display order within the day", example = "10")
        Integer sortOrder
) {
}
