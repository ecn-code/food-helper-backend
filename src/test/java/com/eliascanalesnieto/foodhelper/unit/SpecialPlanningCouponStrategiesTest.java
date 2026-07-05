package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.application.CaprichoPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.application.LuxuryPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.application.OutsidePlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.application.SushiPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpecialPlanningCouponStrategiesTest {
    private final SushiPlanningCouponStrategy sushi = new SushiPlanningCouponStrategy();
    private final OutsidePlanningCouponStrategy outside = new OutsidePlanningCouponStrategy();
    private final CaprichoPlanningCouponStrategy capricho = new CaprichoPlanningCouponStrategy();
    private final LuxuryPlanningCouponStrategy luxury = new LuxuryPlanningCouponStrategy();

    @Test
    void sushiShouldRequireProduct256AndOfferTwentyEurosEveryTwoMonths() {
        ProposedWeekMenu validMenu = menu(day("2026-06-01", section(1L, product(256L))));
        ProposedWeekMenu invalidMenu = menu(day("2026-06-01", section(1L, product(10L))));

        assertThat(sushi.matches(validMenu)).isTrue();
        assertThat(sushi.matches(invalidMenu)).isFalse();
        assertThat(sushi.rewardAmount()).isEqualByComparingTo("20.00");
        assertThat(sushi.periodDays()).isEqualTo(60);
    }

    @Test
    void outsideShouldAlwaysBeAvailableWithThreeMonthCooldown() {
        assertThat(outside.matches(menu(day("2026-06-01", section(1L, product(10L)))))).isTrue();
        assertThat(outside.rewardAmount()).isEqualByComparingTo("20.00");
        assertThat(outside.periodDays()).isEqualTo(90);
    }

    @Test
    void caprichoShouldAlwaysBeAvailableWithThreeMonthCooldown() {
        assertThat(capricho.matches(menu(day("2026-06-01", section(1L, product(10L)))))).isTrue();
        assertThat(capricho.rewardAmount()).isEqualByComparingTo("10.00");
        assertThat(capricho.periodDays()).isEqualTo(90);
    }

    @Test
    void luxuryShouldAlwaysBeAvailableWithSixMonthCooldown() {
        assertThat(luxury.matches(menu(day("2026-06-01", section(1L, product(10L)))))).isTrue();
        assertThat(luxury.rewardAmount()).isEqualByComparingTo("50.00");
        assertThat(luxury.periodDays()).isEqualTo(180);
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
