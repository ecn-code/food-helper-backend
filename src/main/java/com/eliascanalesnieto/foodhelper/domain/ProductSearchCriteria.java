package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.util.StringUtils;

public record ProductSearchCriteria(
        String search,
        BigDecimal caloriesMin,
        BigDecimal caloriesMax,
        BigDecimal carbohydratesMin,
        BigDecimal carbohydratesMax,
        BigDecimal proteinsMin,
        BigDecimal proteinsMax,
        BigDecimal fatsMin,
        BigDecimal fatsMax
) {
    public static ProductSearchCriteria empty() {
        return new ProductSearchCriteria(null, null, null, null, null, null, null, null, null);
    }

    public static ProductSearchCriteria of(
            String search,
            BigDecimal caloriesMin,
            BigDecimal caloriesMax,
            BigDecimal carbohydratesMin,
            BigDecimal carbohydratesMax,
            BigDecimal proteinsMin,
            BigDecimal proteinsMax,
            BigDecimal fatsMin,
            BigDecimal fatsMax
    ) {
        return new ProductSearchCriteria(normalizeSearch(search), caloriesMin, caloriesMax, carbohydratesMin, carbohydratesMax, proteinsMin, proteinsMax, fatsMin, fatsMax);
    }

    public boolean hasFilters() {
        return StringUtils.hasText(search)
                || caloriesMin != null
                || caloriesMax != null
                || carbohydratesMin != null
                || carbohydratesMax != null
                || proteinsMin != null
                || proteinsMax != null
                || fatsMin != null
                || fatsMax != null;
    }

    private static String normalizeSearch(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
