package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;

@Schema(name = "RecipeIngredientResponse", description = "API representation of a recipe ingredient assignment")
public record RecipeIngredientResponse(
        @Schema(description = "Ingredient product identifier", example = "1")
        Long productId,
        @Schema(description = "Ingredient product name", example = "Chicken breast")
        String productName,
        @Schema(description = "Assigned quantity from the ingredient product", example = "125.00")
        BigDecimal quantity,
        @Schema(description = "Quantity unit used for the ingredient", example = "GRAMS")
        QuantityType quantityType,
        @Schema(description = "Calculated nutritional contribution for the assigned grams")
        NutritionalValuesResponse nutritionalValues
) {
}
