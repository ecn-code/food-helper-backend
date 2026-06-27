package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;
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
    @Builder.Default
    List<Supermarket> supermarkets = List.of();
}
