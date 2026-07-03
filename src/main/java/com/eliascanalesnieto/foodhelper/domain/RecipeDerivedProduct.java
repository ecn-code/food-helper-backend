package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecipeDerivedProduct {
    Long productId;
    String name;
    BigDecimal unitsProduced;
    @Builder.Default
    boolean stockFromComposition = true;
    List<RecipeIngredient> ingredients;
}
