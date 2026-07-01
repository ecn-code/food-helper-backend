package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "NutritionalRulesPeriodRequest", description = "Minimum and maximum limits for one nutritional rule period")
public record NutritionalRulesPeriodRequest(
        @Valid @NotNull NutrientRuleRequest calories,
        @Valid @NotNull NutrientRuleRequest carbohydrates,
        @Valid @NotNull NutrientRuleRequest proteins,
        @Valid @NotNull NutrientRuleRequest fats
) {
}
