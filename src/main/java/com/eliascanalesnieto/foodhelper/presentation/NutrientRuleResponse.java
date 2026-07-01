package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "NutrientRuleResponse", description = "Configured limits for one nutrient")
public record NutrientRuleResponse(
        @Schema(description = "Inclusive minimum", nullable = true, example = "100") BigDecimal minimum,
        @Schema(description = "Inclusive maximum", nullable = true, example = "180") BigDecimal maximum
) {
}
