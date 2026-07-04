package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.application.NoRepeatedProductsPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class NoRepeatedProductsPlanningCouponStrategyTest {
    private final NoRepeatedProductsPlanningCouponStrategy strategy = new NoRepeatedProductsPlanningCouponStrategy();

    @Test
    void shouldAllowDistinctProductsAcrossDifferentDaysAndDayParts() {
        ProposedWeekMenu menu = menu(
                day("2026-06-01", section(1L, product(10L), product(11L))),
                day("2026-06-02", section(2L, product(10L)), section(3L, product(12L)))
        );

        assertThat(strategy.matches(menu)).isTrue();
    }

    @Test
    void shouldRejectRepeatedProductOnTheSameDay() {
        ProposedWeekMenu menu = menu(
                day("2026-06-01", section(1L, product(10L)), section(2L, product(10L)))
        );

        assertThat(strategy.matches(menu)).isFalse();
    }

    @Test
    void shouldRejectRepeatedProductOnTheSameDayPartAcrossDifferentDays() {
        ProposedWeekMenu menu = menu(
                day("2026-06-01", section(1L, product(10L))),
                day("2026-06-02", section(1L, product(10L)))
        );

        assertThat(strategy.matches(menu)).isFalse();
    }

    private ProposedWeekMenu menu(ProposedWeekMenuDay... days) {
        return ProposedWeekMenu.builder()
                .days(List.of(days))
                .build();
    }

    private ProposedWeekMenuDay day(String date, ProposedWeekMenuSection... sections) {
        return ProposedWeekMenuDay.builder()
                .date(LocalDate.parse(date))
                .sections(List.of(sections))
                .build();
    }

    private ProposedWeekMenuSection section(Long dayPartId, ProposedWeekMenuProduct... products) {
        return ProposedWeekMenuSection.builder()
                .dayPartId(dayPartId)
                .products(List.of(products))
                .build();
    }

    private ProposedWeekMenuProduct product(Long productId) {
        return ProposedWeekMenuProduct.builder()
                .productId(productId)
                .units(BigDecimal.ONE)
                .grams(BigDecimal.ONE)
                .sortOrder(10)
                .build();
    }
}
