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
    NutritionalValues nutritionalValues;
    Media photo;
}
