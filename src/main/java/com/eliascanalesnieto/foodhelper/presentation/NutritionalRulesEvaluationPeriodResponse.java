package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NutritionalRulesEvaluationPeriodResponse", description = "Evaluation of one nutritional rule period against the saved limits")
public record NutritionalRulesEvaluationPeriodResponse(
        @Schema(description = "Number of planned days used to calculate each average", example = "7") int plannedDays,
        NutrientRuleEvaluationResponse calories,
        NutrientRuleEvaluationResponse carbohydrates,
        NutrientRuleEvaluationResponse proteins,
        NutrientRuleEvaluationResponse fats
) {
}
