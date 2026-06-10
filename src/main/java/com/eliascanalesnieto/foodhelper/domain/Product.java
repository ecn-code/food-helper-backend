package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Product {
    Long id;
    String name;
    String description;
    NutritionalValues nutritionalValues;
}
