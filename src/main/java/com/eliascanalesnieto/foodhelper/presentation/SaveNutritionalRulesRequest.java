package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SaveNutritionalRulesRequest", description = "Daily and weekly nutritional limits used to evaluate planning and menus")
public record SaveNutritionalRulesRequest(
        @Valid @NotNull NutritionalRulesPeriodRequest daily,
        @Valid @NotNull NutritionalRulesPeriodRequest weekly
) {
}
