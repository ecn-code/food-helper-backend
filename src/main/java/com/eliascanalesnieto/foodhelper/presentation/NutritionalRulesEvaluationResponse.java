package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NutritionalRulesEvaluationResponse", description = "Evaluation of average daily nutrition against the saved rules")
public record NutritionalRulesEvaluationResponse(
        @Schema(description = "Number of planned days used to calculate each average", example = "7") int plannedDays,
        NutrientRuleEvaluationResponse calories,
        NutrientRuleEvaluationResponse carbohydrates,
        NutrientRuleEvaluationResponse proteins,
        NutrientRuleEvaluationResponse fats
) {
}
