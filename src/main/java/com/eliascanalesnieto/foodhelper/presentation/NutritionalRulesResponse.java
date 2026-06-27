package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NutritionalRulesResponse", description = "Saved daily nutritional rules")
public record NutritionalRulesResponse(
        NutrientRuleResponse calories,
        NutrientRuleResponse carbohydrates,
        NutrientRuleResponse proteins,
        NutrientRuleResponse fats
) {
}
