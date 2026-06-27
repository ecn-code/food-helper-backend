package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SaveNutritionalRulesRequest", description = "Daily nutritional limits used to evaluate planning and menus")
public record SaveNutritionalRulesRequest(
        @Valid @NotNull NutrientRuleRequest calories,
        @Valid @NotNull NutrientRuleRequest carbohydrates,
        @Valid @NotNull NutrientRuleRequest proteins,
        @Valid @NotNull NutrientRuleRequest fats
) {
}
