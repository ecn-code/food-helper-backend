package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "RecipeResponse", description = "API representation of a recipe")
public record RecipeResponse(
        @Schema(description = "Recipe identifier", example = "1")
        Long id,
        @Schema(description = "Recipe name", example = "Chicken curry")
        String name,
        @Schema(description = "Recipe description", example = "Creamy curry made with chicken and spices")
        String description,
        @Schema(description = "Recipe preparation instructions", example = "Saute vegetables, add chicken, then simmer.")
        String instructions,
        @Schema(description = "Recipe nutritional totals calculated from the assigned ingredient grams")
        NutritionalValuesResponse nutritionalValues,
        @Schema(description = "Assigned ingredient products")
        List<RecipeIngredientResponse> products,
        @Schema(description = "Linked derived product information when the recipe has created one")
        RecipeDerivedProductResponse derivedProduct,
        @Schema(description = "Optional photo metadata")
        MediaResponse photo
) {
}
