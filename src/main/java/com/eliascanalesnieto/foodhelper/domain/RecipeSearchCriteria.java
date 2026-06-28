package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;

public record RecipeSearchCriteria(
        String search,
        BigDecimal minCalories,
        BigDecimal maxCalories,
        BigDecimal minCarbohydrates,
        BigDecimal maxCarbohydrates,
        BigDecimal minProteins,
        BigDecimal maxProteins,
        BigDecimal minFats,
        BigDecimal maxFats,
        Boolean hasDerivedProduct
) {
    public RecipeSearchCriteria {
        search = search == null || search.isBlank() ? null : search.trim();
        validateRange("calories", minCalories, maxCalories);
        validateRange("carbohydrates", minCarbohydrates, maxCarbohydrates);
        validateRange("proteins", minProteins, maxProteins);
        validateRange("fats", minFats, maxFats);
    }

    public static RecipeSearchCriteria empty() {
        return new RecipeSearchCriteria(null, null, null, null, null, null, null, null, null, null);
    }

    private static void validateRange(String name, BigDecimal min, BigDecimal max) {
        if ((min != null && min.signum() < 0) || (max != null && max.signum() < 0)) {
            throw new IllegalArgumentException(name + " range cannot be negative");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            throw new IllegalArgumentException("min" + suffix + " cannot be greater than max" + suffix);
        }
    }
}
