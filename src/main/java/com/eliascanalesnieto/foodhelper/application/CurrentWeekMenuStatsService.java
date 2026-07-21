package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuRangeStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStockItemResponse;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
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

    public CurrentWeekMenuRangeStatsResponse summarizeRange(List<CurrentWeekMenuResponse> menus, LocalDate from, LocalDate to) {
        validateRange(from, to);

        List<FilteredMenu> selectedMenus = menus.stream()
                .map(menu -> new FilteredMenu(menu, filterMenu(menu, from, to)))
                .filter(menu -> !menu.included().days().isEmpty())
                .toList();

        if (selectedMenus.isEmpty()) {
            return new CurrentWeekMenuRangeStatsResponse(from, to, 0, ZERO, 0, ZERO, ZERO, List.of());
        }

        List<ProposedWeekMenuDayResponse> days = selectedMenus.stream()
                .flatMap(menu -> menu.included().days().stream())
                .toList();

        BigDecimal calories = days.stream()
                .map(day -> safeNutrition(day).calories())
                .map(this::normalize)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<Long> distinctProductIds = days.stream()
                .flatMap(day -> safeSections(day).stream())
                .flatMap(section -> safeProducts(section).stream())
                .map(ProposedWeekMenuProductResponse::productId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        BigDecimal estimatedCost = selectedMenus.stream()
                .map(this::proratedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal averageCaloriesPerPlannedDay = calories.divide(
                BigDecimal.valueOf(days.size()), SCALE, RoundingMode.HALF_UP);

        return new CurrentWeekMenuRangeStatsResponse(
                from,
                to,
                days.size(),
                calories.setScale(SCALE, RoundingMode.HALF_UP),
                distinctProductIds.size(),
                estimatedCost,
                averageCaloriesPerPlannedDay,
                selectedMenus.stream().map(menu -> menu.original().id()).toList()
        );
    }

    /** Returns the menu cost apportioned to the days included in the requested range. */
    private BigDecimal proratedCost(FilteredMenu menu) {
        long plannedDays = Math.max(1, safeDays(menu.original()).size());
        long includedDays = safeDays(menu.included()).size();
        BigDecimal fullMenuCost = summarize(List.of(menu.original())).moneySpent();
        return fullMenuCost.multiply(BigDecimal.valueOf(includedDays))
                .divide(BigDecimal.valueOf(plannedDays), SCALE, RoundingMode.HALF_UP);
    }

    private record FilteredMenu(CurrentWeekMenuResponse original, CurrentWeekMenuResponse included) {
    }

    private Comparator<CurrentWeekMenuPeriodStatsDayResponse> dayComparator() {
        return Comparator
                .comparing(CurrentWeekMenuPeriodStatsDayResponse::calories)
                .thenComparing(CurrentWeekMenuPeriodStatsDayResponse::date);
    }

    private BigDecimal average(BigDecimal total, int count) {
        return total.divide(BigDecimal.valueOf(count), SCALE, RoundingMode.HALF_UP);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Range start and end are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Range start must not be after range end");
        }
    }

    private CurrentWeekMenuResponse filterMenu(CurrentWeekMenuResponse menu, LocalDate from, LocalDate to) {
        List<ProposedWeekMenuDayResponse> days = safeDays(menu).stream()
                .filter(day -> !day.date().isBefore(from) && !day.date().isAfter(to))
                .toList();
        return new CurrentWeekMenuResponse(
                menu.id(),
                menu.planningId(),
                menu.payerUserId(),
                menu.payerUsername(),
                safePersonIds(menu),
                menu.startDate(),
                menu.endDate(),
                days,
                menu.nutritionalValues(),
                menu.stockSummary(),
                menu.usedStock(),
                safeWeekStock(menu),
                menu.shoppingList(),
                safeStockMovements(menu),
                safeRecipeProductions(menu),
                menu.nutritionalRules(),
                menu.state()
        );
    }

    private NutritionalValuesResponse safeNutrition(ProposedWeekMenuDayResponse day) {
        return day.nutritionalValues() == null
                ? new NutritionalValuesResponse(ZERO, ZERO, ZERO, ZERO)
                : day.nutritionalValues();
    }

    private List<ProposedWeekMenuSectionResponse> safeSections(ProposedWeekMenuDayResponse day) {
        return day.sections() == null ? List.of() : day.sections();
    }

    private List<ProposedWeekMenuProductResponse> safeProducts(ProposedWeekMenuSectionResponse section) {
        return section.products() == null ? List.of() : section.products();
    }

    private List<Long> safePersonIds(CurrentWeekMenuResponse menu) {
        return menu.personIds() == null ? List.of() : menu.personIds();
    }

    private List<ProposedWeekMenuDayResponse> safeDays(CurrentWeekMenuResponse menu) {
        return menu.days() == null ? List.of() : menu.days();
    }

    private List<com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse> safeUsedStock(CurrentWeekMenuResponse menu) {
        return menu.usedStock() == null ? List.of() : menu.usedStock();
    }

    private List<CurrentWeekMenuStockItemResponse> safeWeekStock(CurrentWeekMenuResponse menu) {
        return menu.weekStock() == null ? List.of() : menu.weekStock();
    }

    private List<MenuStockMovementResponse> safeStockMovements(CurrentWeekMenuResponse menu) {
        return menu.stockMovements() == null ? List.of() : menu.stockMovements();
    }

    private List<com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuRecipeProductionResponse> safeRecipeProductions(CurrentWeekMenuResponse menu) {
        return menu.recipeProductions() == null ? List.of() : menu.recipeProductions();
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
