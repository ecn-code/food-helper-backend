package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Product {
    Long id;
    String name;
    String description;
    java.math.BigDecimal gramsPerUnit;
    java.math.BigDecimal defaultPrice;
    NutritionalValues nutritionalValues;
    Media photo;
}
