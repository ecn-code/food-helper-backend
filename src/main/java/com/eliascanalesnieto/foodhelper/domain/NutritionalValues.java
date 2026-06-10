package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NutritionalValues {
    Long productId;
    BigDecimal calories;
    BigDecimal carbohydrates;
    BigDecimal proteins;
    BigDecimal fats;
}
