package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "NutrientRuleRequest", description = "Optional minimum and maximum for one nutrient")
public record NutrientRuleRequest(
        @Schema(description = "Inclusive minimum", nullable = true, example = "100")
        @PositiveOrZero BigDecimal minimum,
        @Schema(description = "Inclusive maximum", nullable = true, example = "180")
        @PositiveOrZero BigDecimal maximum
) {
}
