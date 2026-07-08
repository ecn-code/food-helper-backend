package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NoRepeatedProductsPlanningCouponStrategy implements PlanningCouponStrategy {
    private static final BigDecimal REWARD_AMOUNT = new BigDecimal("15.00");
    private static final int PERIOD_DAYS = 30;

    @Override
    public String code() {
        return "NO_REPEATED_PRODUCTS";
    }

    @Override
    public String name() {
        return "No repeated products";
    }

    @Override
    public String conditionDescription() {
        return "The menu must fill every planned day with at least 3 products and cannot repeat the same product on the same day or in the same day part across different days";
    }

    @Override
    public int periodDays() {
        return PERIOD_DAYS;
    }

    @Override
    public BigDecimal rewardAmount() {
        return REWARD_AMOUNT;
    }

    @Override
    public boolean matches(ProposedWeekMenu proposedMenu) {
        if (!hasEnoughProductsPerDay(proposedMenu)) {
            return false;
        }
        if (!coversAllPlannedDays(proposedMenu)) {
            return false;
        }
        Map<Long, List<Appearance>> appearancesByProduct = new HashMap<>();
        for (ProposedWeekMenuDay day : safeDays(proposedMenu)) {
            for (ProposedWeekMenuSection section : safeSections(day)) {
                for (ProposedWeekMenuProduct product : safeProducts(section)) {
                    if (product.getProductId() == null) {
                        continue;
                    }
                    appearancesByProduct.computeIfAbsent(product.getProductId(), ignored -> new java.util.ArrayList<>())
                            .add(new Appearance(day.getDate(), section.getDayPartId()));
                }
            }
        }
        return appearancesByProduct.values().stream().noneMatch(this::violatesRule);
    }

    private boolean hasEnoughProductsPerDay(ProposedWeekMenu proposedMenu) {
        return safeDays(proposedMenu).stream()
                .allMatch(day -> countProducts(day) >= 3);
    }

    private boolean coversAllPlannedDays(ProposedWeekMenu proposedMenu) {
        LocalDate startDate = proposedMenu.getStartDate();
        LocalDate endDate = proposedMenu.getEndDate();
        if (startDate == null || endDate == null) {
            return true;
        }
        if (endDate.isBefore(startDate)) {
            return false;
        }
        Set<LocalDate> plannedDates = new HashSet<>();
        for (ProposedWeekMenuDay day : safeDays(proposedMenu)) {
            if (day.getDate() == null) {
                return false;
            }
            plannedDates.add(day.getDate());
        }
        long expectedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (plannedDates.size() != expectedDays) {
            return false;
        }
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!plannedDates.contains(current)) {
                return false;
            }
            current = current.plusDays(1);
        }
        return true;
    }

    private int countProducts(ProposedWeekMenuDay day) {
        int count = 0;
        for (ProposedWeekMenuSection section : safeSections(day)) {
            for (ProposedWeekMenuProduct product : safeProducts(section)) {
                if (product.getProductId() != null) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean violatesRule(List<Appearance> appearances) {
        if (appearances.size() < 2) {
            return false;
        }
        Set<java.time.LocalDate> dates = new HashSet<>();
        Set<Long> dayPartIds = new HashSet<>();
        for (Appearance appearance : appearances) {
            if (!dates.add(appearance.date()) || !dayPartIds.add(appearance.dayPartId())) {
                return true;
            }
        }
        return false;
    }

    private List<ProposedWeekMenuDay> safeDays(ProposedWeekMenu proposedMenu) {
        return proposedMenu.getDays() == null ? List.of() : proposedMenu.getDays();
    }

    private List<ProposedWeekMenuSection> safeSections(ProposedWeekMenuDay day) {
        return day.getSections() == null ? List.of() : day.getSections();
    }

    private List<ProposedWeekMenuProduct> safeProducts(ProposedWeekMenuSection section) {
        return section.getProducts() == null ? List.of() : section.getProducts();
    }

    private record Appearance(java.time.LocalDate date, Long dayPartId) {
    }
}
