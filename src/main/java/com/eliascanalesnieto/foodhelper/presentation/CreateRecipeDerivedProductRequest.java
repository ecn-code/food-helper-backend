package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "CreateRecipeDerivedProductRequest", description = "Payload for creating a product derived from a recipe")
public record CreateRecipeDerivedProductRequest(
        @Schema(description = "Total grams of product yielded by the recipe", example = "400")
        @NotNull @Positive BigDecimal producedGrams,
        @Schema(description = "Grams represented by one product unit", example = "100")
        @NotNull @Positive BigDecimal gramsPerUnit
) {
}
