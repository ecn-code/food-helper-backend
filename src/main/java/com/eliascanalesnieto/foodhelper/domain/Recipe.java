package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Recipe {
    Long id;
    String name;
    String description;
    String instructions;
    BigDecimal defaultUnitsProduced;
    List<RecipeIngredient> ingredients;
    NutritionalValues nutritionalValues;
    RecipeDerivedProduct derivedProduct;
    Media photo;
}
