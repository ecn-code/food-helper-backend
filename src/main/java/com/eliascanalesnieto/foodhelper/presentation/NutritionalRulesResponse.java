package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NutritionalRulesResponse", description = "Saved daily and weekly nutritional rules")
public record NutritionalRulesResponse(
        NutritionalRulesPeriodResponse daily,
        NutritionalRulesPeriodResponse weekly
) {
}
