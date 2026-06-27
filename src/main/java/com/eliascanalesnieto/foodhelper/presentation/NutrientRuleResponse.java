package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "NutrientRuleResponse", description = "Configured daily limits for one nutrient")
public record NutrientRuleResponse(
        @Schema(description = "Inclusive daily minimum", nullable = true, example = "100") BigDecimal minimum,
        @Schema(description = "Inclusive daily maximum", nullable = true, example = "180") BigDecimal maximum
) {
}
