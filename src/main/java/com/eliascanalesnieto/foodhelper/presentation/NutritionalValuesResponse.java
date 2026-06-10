package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "NutritionalValuesResponse", description = "Nutritional values per serving")
public record NutritionalValuesResponse(
        @Schema(description = "Calories", example = "52")
        BigDecimal calories,
        @Schema(description = "Carbohydrates", example = "14")
        BigDecimal carbohydrates,
        @Schema(description = "Proteins", example = "0.3")
        BigDecimal proteins,
        @Schema(description = "Fats", example = "0.2")
        BigDecimal fats
) {
}
