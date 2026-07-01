package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NutritionalRulesEvaluationResponse", description = "Evaluation of average daily nutrition against the saved daily and weekly rules")
public record NutritionalRulesEvaluationResponse(
        NutritionalRulesEvaluationPeriodResponse daily,
        NutritionalRulesEvaluationPeriodResponse weekly
) {
}
