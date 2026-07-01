package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NutritionalRulesPeriodResponse", description = "Minimum and maximum limits for one nutritional rule period")
public record NutritionalRulesPeriodResponse(
        NutrientRuleResponse calories,
        NutrientRuleResponse carbohydrates,
        NutrientRuleResponse proteins,
        NutrientRuleResponse fats
) {
}
