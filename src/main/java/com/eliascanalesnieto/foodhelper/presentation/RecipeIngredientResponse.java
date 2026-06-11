package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "RecipeIngredientResponse", description = "API representation of a recipe ingredient assignment")
public record RecipeIngredientResponse(
        @Schema(description = "Ingredient product identifier", example = "1")
        Long productId,
        @Schema(description = "Ingredient product name", example = "Chicken breast")
        String productName,
        @Schema(description = "Assigned grams from the ingredient product", example = "125.00")
        BigDecimal grams,
        @Schema(description = "Calculated nutritional contribution for the assigned grams")
        NutritionalValuesResponse nutritionalValues
) {
}
