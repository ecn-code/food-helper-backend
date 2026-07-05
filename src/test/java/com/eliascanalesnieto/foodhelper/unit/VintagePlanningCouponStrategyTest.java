package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.VintagePlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.UserMenuHistoryRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VintagePlanningCouponStrategyTest {
    private final UserMenuHistoryRepository userMenuHistoryRepository = Mockito.mock(UserMenuHistoryRepository.class);
    private final VintagePlanningCouponStrategy strategy = new VintagePlanningCouponStrategy(userMenuHistoryRepository);

    @Test
    void shouldAllowMenuWhenAProductWasNotUsedInTheLastTwoMonths() {
        ProposedWeekMenu menu = menu(day("2026-07-05", section(1L, product(10L))));
        when(userMenuHistoryRepository.findMenus(7L, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 7, 5)))
                .thenReturn(List.of(historyMenu(11L)));

        assertThat(strategy.matches(menu, 7L, Instant.parse("2026-07-05T10:00:00Z"))).isTrue();
    }

    @Test
    void shouldRejectMenuWhenAllProductsWereRecentlyUsed() {
        ProposedWeekMenu menu = menu(day("2026-07-05", section(1L, product(10L))));
        when(userMenuHistoryRepository.findMenus(7L, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 7, 5)))
                .thenReturn(List.of(historyMenu(10L)));

        assertThat(strategy.matches(menu, 7L, Instant.parse("2026-07-05T10:00:00Z"))).isFalse();
    }

    @Test
    void shouldRejectEmptyMenus() {
        assertThat(strategy.matches(ProposedWeekMenu.builder().days(List.of()).build(), 7L, Instant.parse("2026-07-05T10:00:00Z"))).isFalse();
    }

    private CurrentWeekMenuResponse historyMenu(Long productId) {
        return new CurrentWeekMenuResponse(
                1L,
                2L,
                7L,
                "payer",
                List.of(7L),
                LocalDate.of(2026, 5, 6),
                LocalDate.of(2026, 5, 12),
                List.of(new ProposedWeekMenuDayResponse(
                        1L,
                        LocalDate.of(2026, 5, 6),
                        List.of(new ProposedWeekMenuSectionResponse(
                                1L,
                                1L,
                                "Lunch",
                                "Lunch",
                                10,
                                List.of(new ProposedWeekMenuProductResponse(productId, "Product " + productId, BigDecimal.ONE, BigDecimal.ONE, 10, null)),
                                null
                        )),
                        List.of(),
                        null
                )),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null
        );
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
