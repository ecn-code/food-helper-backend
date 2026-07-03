package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuRecipeProduction {
    Long id;
    Long recipeId;
    String recipeName;
    Long productId;
    String productName;
    BigDecimal units;
    BigDecimal grams;
    Integer sortOrder;
}
