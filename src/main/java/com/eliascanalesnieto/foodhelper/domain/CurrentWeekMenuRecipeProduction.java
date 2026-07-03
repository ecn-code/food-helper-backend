package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CurrentWeekMenuRecipeProduction {
    Long id;
    Long recipeId;
    String recipeName;
    Long productId;
    String productName;
    BigDecimal units;
    BigDecimal grams;
    Integer sortOrder;
    boolean transferred;
    String transferType;
    Long stockEntryId;
    LocalDateTime transferredAt;
}
