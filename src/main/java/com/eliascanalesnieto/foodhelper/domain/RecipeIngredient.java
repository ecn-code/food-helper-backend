package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecipeIngredient {
    Long productId;
    String productName;
    BigDecimal quantity;
    QuantityType quantityType;
    NutritionalValues nutritionalValues;
}
