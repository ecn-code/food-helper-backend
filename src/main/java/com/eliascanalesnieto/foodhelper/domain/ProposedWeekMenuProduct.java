package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuProduct {
    Long productId;
    String productName;
    BigDecimal units;
    BigDecimal grams;
    Integer sortOrder;
    NutritionalValues nutritionalValues;
}
