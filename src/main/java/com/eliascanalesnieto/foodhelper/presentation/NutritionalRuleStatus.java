package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of comparing an observed daily average with a nutritional rule")
public enum NutritionalRuleStatus {
    BELOW_MINIMUM,
    WITHIN_RANGE,
    ABOVE_MAXIMUM,
    NOT_CONFIGURED
}
