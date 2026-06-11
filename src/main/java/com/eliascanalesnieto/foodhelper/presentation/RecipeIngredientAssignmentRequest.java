package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "RecipeIngredientAssignmentRequest", description = "Ingredient assignment for a recipe")
public record RecipeIngredientAssignmentRequest(
        @Schema(description = "Ingredient product identifier", example = "1")
        @NotNull Long productId,
        @Schema(description = "Assigned grams from the ingredient product", example = "125")
        @NotNull @Positive BigDecimal grams
) {
}
