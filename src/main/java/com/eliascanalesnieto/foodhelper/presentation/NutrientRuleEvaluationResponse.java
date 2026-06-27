package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "NutrientRuleEvaluationResponse", description = "Daily average and its position relative to the saved rule")
public record NutrientRuleEvaluationResponse(
        @Schema(description = "Observed average per planned day", example = "125.50") BigDecimal value,
        @Schema(description = "Configured inclusive minimum", nullable = true, example = "100") BigDecimal minimum,
        @Schema(description = "Configured inclusive maximum", nullable = true, example = "180") BigDecimal maximum,
        NutritionalRuleStatus status
) {
}
