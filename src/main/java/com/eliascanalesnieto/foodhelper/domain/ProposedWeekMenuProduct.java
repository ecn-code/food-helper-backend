package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ProposedWeekMenuProduct {
    Long productId;
    String productName;
    BigDecimal units;
    BigDecimal grams;
    Integer sortOrder;
    NutritionalValues nutritionalValues;
}
