package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder
@Jacksonized
public class NutritionalValues {
    Long productId;
    BigDecimal calories;
    BigDecimal carbohydrates;
    BigDecimal proteins;
    BigDecimal fats;
}
