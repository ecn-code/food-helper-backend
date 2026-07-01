package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuStatsService;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CurrentWeekMenuStatsServiceTest {
    private final CurrentWeekMenuStatsService service = new CurrentWeekMenuStatsService();

    @Test
    void shouldSummarizeWeekAndMonthStatistics() {
        CurrentWeekMenuResponse closedWeek = menu(
                10L,
                LocalDate.of(2026, 6, 17),
                day(LocalDate.of(2026, 6, 17), "1000", "90", "70", "30"),
                List.of(usedStock("12.50")),
                List.of(stockMovement("5.00"))
        );
        CurrentWeekMenuResponse olderClosedWeek = menu(
                11L,
                LocalDate.of(2026, 6, 10),
                day(LocalDate.of(2026, 6, 10), "500", "50", "40", "20"),
                List.of(usedStock("7.50")),
                List.of(stockMovement("2.50"))
        );

        CurrentWeekMenuStatsResponse stats = service.build(closedWeek, List.of(closedWeek, olderClosedWeek));

        assertThat(stats.menuId()).isEqualTo(10L);
        assertThat(stats.period()).isEqualTo(new CurrentWeekMenuPeriodStatsResponse(
                new com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsDayResponse(LocalDate.of(2026, 6, 17), new BigDecimal("1000.00")),
                new com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsDayResponse(LocalDate.of(2026, 6, 17), new BigDecimal("1000.00")),
                new BigDecimal("1000.00"),
                new BigDecimal("90.00"),
                new BigDecimal("70.00"),
                new BigDecimal("30.00"),
                new BigDecimal("17.50")
        ));
        assertThat(stats.month()).isEqualTo(new CurrentWeekMenuPeriodStatsResponse(
                new com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsDayResponse(LocalDate.of(2026, 6, 17), new BigDecimal("1000.00")),
                new com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsDayResponse(LocalDate.of(2026, 6, 10), new BigDecimal("500.00")),
                new BigDecimal("750.00"),
                new BigDecimal("70.00"),
                new BigDecimal("55.00"),
                new BigDecimal("25.00"),
                new BigDecimal("27.50")
        ));
    }

    private CurrentWeekMenuResponse menu(
            Long id,
            LocalDate endDate,
            ProposedWeekMenuDayResponse day,
            List<CurrentWeekMenuUsedStockResponse> usedStock,
            List<MenuStockMovementResponse> stockMovements
    ) {
        return new CurrentWeekMenuResponse(
                id,
                5L,
                1L,
                "elias",
                List.of(),
                endDate.minusDays(7),
                endDate,
                List.of(day),
                new NutritionalValuesResponse(
                        day.nutritionalValues().calories(),
                        day.nutritionalValues().carbohydrates(),
                        day.nutritionalValues().proteins(),
                        day.nutritionalValues().fats()
                ),
                null,
                usedStock,
                List.of(),
                stockMovements,
                List.of(),
                null
        );
    }

    private ProposedWeekMenuDayResponse day(LocalDate date, String calories, String carbohydrates, String proteins, String fats) {
        return new ProposedWeekMenuDayResponse(
                1L,
                date,
                List.of(new ProposedWeekMenuSectionResponse(1L, 1L, "Lunch", "", 1, List.of(), new NutritionalValuesResponse(
                        new BigDecimal(calories),
                        new BigDecimal(carbohydrates),
                        new BigDecimal(proteins),
                        new BigDecimal(fats)
                ))),
                List.of(),
                new NutritionalValuesResponse(
                        new BigDecimal(calories),
                        new BigDecimal(carbohydrates),
                        new BigDecimal(proteins),
                        new BigDecimal(fats)
                )
        );
    }

    private CurrentWeekMenuUsedStockResponse usedStock(String totalCost) {
        return new CurrentWeekMenuUsedStockResponse(
                1L,
                1L,
                "Item",
                new BigDecimal("1.00"),
                new BigDecimal("12.50"),
                new BigDecimal(totalCost),
                null,
                null
        );
    }

    private MenuStockMovementResponse stockMovement(String totalCost) {
        return new MenuStockMovementResponse(
                1L,
                10L,
                1L,
                "payer",
                99L,
                "Rice",
                new BigDecimal("1.00"),
                new BigDecimal("5.00"),
                new BigDecimal(totalCost),
                "Weekly groceries",
                java.time.LocalDateTime.of(2026, 6, 1, 10, 0)
        );
    }
}
