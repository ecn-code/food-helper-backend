package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NutritionalRuleSet {
    BigDecimal caloriesMinimum;
    BigDecimal caloriesMaximum;
    BigDecimal carbohydratesMinimum;
    BigDecimal carbohydratesMaximum;
    BigDecimal proteinsMinimum;
    BigDecimal proteinsMaximum;
    BigDecimal fatsMinimum;
    BigDecimal fatsMaximum;
}
