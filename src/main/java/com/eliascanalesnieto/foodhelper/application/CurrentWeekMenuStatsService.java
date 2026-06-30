package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockMovementResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CurrentWeekMenuStatsService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final int SCALE = 2;

    public CurrentWeekMenuStatsResponse build(CurrentWeekMenuResponse closedWeek, List<CurrentWeekMenuResponse> closedWeeks) {
        YearMonth month = YearMonth.from(closedWeek.endDate());
        List<CurrentWeekMenuResponse> sameMonthWeeks = closedWeeks.stream()
                .filter(menu -> YearMonth.from(menu.endDate()).equals(month))
                .toList();

        return new CurrentWeekMenuStatsResponse(
                closedWeek.id(),
                summarize(List.of(closedWeek)),
                summarize(sameMonthWeeks)
        );
    }

    public CurrentWeekMenuPeriodStatsResponse summarize(List<CurrentWeekMenuResponse> menus) {
        List<CurrentWeekMenuPeriodStatsDayResponse> days = menus.stream()
                .flatMap(menu -> menu.days().stream())
                .map(day -> new CurrentWeekMenuPeriodStatsDayResponse(
                        day.date(),
                        day.nutritionalValues().calories().setScale(SCALE, RoundingMode.HALF_UP)
                ))
                .toList();

        BigDecimal totalCalories = days.stream().map(CurrentWeekMenuPeriodStatsDayResponse::calories).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCarbohydrates = menus.stream()
                .flatMap(menu -> menu.days().stream())
                .map(day -> day.nutritionalValues().carbohydrates())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProteins = menus.stream()
                .flatMap(menu -> menu.days().stream())
                .map(day -> day.nutritionalValues().proteins())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFats = menus.stream()
                .flatMap(menu -> menu.days().stream())
                .map(day -> day.nutritionalValues().fats())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMoneySpent = menus.stream()
                .flatMap(menu -> safeUsedStock(menu).stream())
                .map(item -> item.totalCost() == null ? ZERO : item.totalCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRepercuted = menus.stream()
                .flatMap(menu -> safeStockMovements(menu).stream())
                .map(MenuStockMovementResponse::totalCost)
                .map(total -> total == null ? ZERO : total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int dayCount = days.size();
        if (dayCount == 0) {
            return new CurrentWeekMenuPeriodStatsResponse(null, null, ZERO, ZERO, ZERO, ZERO, ZERO);
        }

        CurrentWeekMenuPeriodStatsDayResponse maxDay = days.stream()
                .max(dayComparator())
                .orElse(null);
        CurrentWeekMenuPeriodStatsDayResponse minDay = days.stream()
                .min(dayComparator())
                .orElse(null);

        return new CurrentWeekMenuPeriodStatsResponse(
                maxDay,
                minDay,
                average(totalCalories, dayCount),
                average(totalCarbohydrates, dayCount),
                average(totalProteins, dayCount),
                average(totalFats, dayCount),
                totalMoneySpent.add(totalRepercuted).setScale(SCALE, RoundingMode.HALF_UP)
        );
    }

    private Comparator<CurrentWeekMenuPeriodStatsDayResponse> dayComparator() {
        return Comparator
                .comparing(CurrentWeekMenuPeriodStatsDayResponse::calories)
                .thenComparing(CurrentWeekMenuPeriodStatsDayResponse::date);
    }

    private BigDecimal average(BigDecimal total, int count) {
        return total.divide(BigDecimal.valueOf(count), SCALE, RoundingMode.HALF_UP);
    }

    private List<com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse> safeUsedStock(CurrentWeekMenuResponse menu) {
        return menu.usedStock() == null ? List.of() : menu.usedStock();
    }

    private List<MenuStockMovementResponse> safeStockMovements(CurrentWeekMenuResponse menu) {
        return menu.stockMovements() == null ? List.of() : menu.stockMovements();
    }
}
