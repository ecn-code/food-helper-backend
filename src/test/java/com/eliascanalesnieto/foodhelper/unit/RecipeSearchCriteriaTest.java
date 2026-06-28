package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eliascanalesnieto.foodhelper.domain.RecipeSearchCriteria;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RecipeSearchCriteriaTest {

    @Test
    void shouldNormalizeBlankSearchAndAcceptInclusiveRanges() {
        RecipeSearchCriteria criteria = new RecipeSearchCriteria(
                "  ", new BigDecimal("10"), new BigDecimal("10"),
                null, null, null, null, null, null, true
        );

        assertThat(criteria.search()).isNull();
        assertThat(criteria.minCalories()).isEqualByComparingTo("10");
        assertThat(criteria.maxCalories()).isEqualByComparingTo("10");
        assertThat(criteria.hasDerivedProduct()).isTrue();
    }

    @Test
    void shouldRejectNegativeAndInvertedRanges() {
        assertThatThrownBy(() -> new RecipeSearchCriteria(
                null, new BigDecimal("-1"), null, null, null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new RecipeSearchCriteria(
                null, null, null, null, null, new BigDecimal("20"), new BigDecimal("10"), null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minProteins cannot be greater than maxProteins");
    }
}
