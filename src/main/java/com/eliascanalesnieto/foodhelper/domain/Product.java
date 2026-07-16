package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Product {
    Long id;
    String name;
    String description;
    java.math.BigDecimal gramsPerUnit;
    @Builder.Default
    boolean stockInUnits = false;
    java.math.BigDecimal defaultPrice;
    Instant createdAt;
    NutritionBasis nutritionBasis;
    NutritionalValues nutritionalValues;
    Media photo;
    RecipeDerivedProduct derivedProduct;
    @Builder.Default
    List<Supermarket> supermarkets = List.of();
}
