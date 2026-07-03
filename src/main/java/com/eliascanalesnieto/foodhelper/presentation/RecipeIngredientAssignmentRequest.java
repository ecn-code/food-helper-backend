package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import java.math.BigDecimal;

@Schema(name = "RecipeIngredientAssignmentRequest", description = "Ingredient assignment for a recipe")
public record RecipeIngredientAssignmentRequest(
        @Schema(description = "Ingredient product identifier", example = "1")
        @NotNull Long productId,
        @Schema(description = "Assigned quantity from the ingredient product", example = "1.5")
        @NotNull @Positive BigDecimal quantity,
        @Schema(description = "Quantity unit used for the ingredient", example = "GRAMS")
        @NotNull QuantityType quantityType
) {
    public RecipeIngredientAssignmentRequest(Long productId, BigDecimal grams) {
        this(productId, grams, QuantityType.GRAMS);
    }
}
