package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NutritionalRules {
    NutritionalRuleSet daily;
    NutritionalRuleSet weekly;
}
