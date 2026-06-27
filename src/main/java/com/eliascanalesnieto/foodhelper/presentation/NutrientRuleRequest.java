package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "NutrientRuleRequest", description = "Optional daily minimum and maximum for one nutrient")
public record NutrientRuleRequest(
        @Schema(description = "Inclusive daily minimum", nullable = true, example = "100")
        @PositiveOrZero BigDecimal minimum,
        @Schema(description = "Inclusive daily maximum", nullable = true, example = "180")
        @PositiveOrZero BigDecimal maximum
) {
}
