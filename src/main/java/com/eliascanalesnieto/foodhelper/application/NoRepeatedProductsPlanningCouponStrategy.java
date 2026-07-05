package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import java.math.BigDecimal;
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
        return "The menu cannot repeat the same product on the same day or in the same day part across different days";
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
