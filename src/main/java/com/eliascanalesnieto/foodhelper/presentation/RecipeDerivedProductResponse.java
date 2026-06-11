package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "RecipeDerivedProductResponse", description = "Derived product information linked to a recipe")
public record RecipeDerivedProductResponse(
        @Schema(description = "Derived product identifier", example = "7")
        Long productId,
        @Schema(description = "Total grams yielded by the recipe", example = "400.00")
        BigDecimal producedGrams,
        @Schema(description = "Grams represented by one product unit", example = "100.00")
        BigDecimal gramsPerUnit,
        @Schema(description = "Calculated number of units yielded by the recipe", example = "4.00")
        BigDecimal unitsProduced
) {
}
