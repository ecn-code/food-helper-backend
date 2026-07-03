package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "RecipeDerivedProductResponse", description = "Derived product information linked to a recipe")
public record RecipeDerivedProductResponse(
        @Schema(description = "Derived product identifier", example = "7")
        Long productId,
        @Schema(description = "Derived product name", example = "Curry base")
        String name,
        @Schema(description = "Calculated number of units yielded by the recipe", example = "4.00")
        BigDecimal unitsProduced,
        @Schema(description = "Whether stock is expanded into the recipe ingredients instead of tracked as this product itself", example = "true")
        boolean stockFromComposition,
        @Schema(description = "Per-unit ingredient composition of the derived product")
        List<RecipeIngredientResponse> ingredients
) {
}
