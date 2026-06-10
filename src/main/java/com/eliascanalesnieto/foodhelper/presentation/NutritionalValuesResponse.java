package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "NutritionalValuesResponse", description = "Valores nutricionales por porcion")
public record NutritionalValuesResponse(
        @Schema(description = "Calorias", example = "52")
        BigDecimal calories,
        @Schema(description = "Carbohidratos", example = "14")
        BigDecimal carbohydrates,
        @Schema(description = "Proteinas", example = "0.3")
        BigDecimal proteins,
        @Schema(description = "Grasas", example = "0.2")
        BigDecimal fats
) {
}
