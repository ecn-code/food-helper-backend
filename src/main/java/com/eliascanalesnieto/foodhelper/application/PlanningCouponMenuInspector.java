package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuRecipeProductionResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class PlanningCouponMenuInspector {
    private PlanningCouponMenuInspector() {
    }

    static Set<Long> productIdsFrom(ProposedWeekMenu menu) {
        Set<Long> productIds = new LinkedHashSet<>();
        for (ProposedWeekMenuDay day : safeDays(menu)) {
            for (ProposedWeekMenuSection section : safeSections(day)) {
                for (ProposedWeekMenuProduct product : safeProducts(section)) {
                    if (product.getProductId() != null) {
                        productIds.add(product.getProductId());
                    }
                }
            }
            for (ProposedWeekMenuRecipeProduction production : safeProductions(day)) {
                if (production.getProductId() != null) {
                    productIds.add(production.getProductId());
                }
            }
        }
        return productIds;
    }

    static Set<Long> productIdsFrom(CurrentWeekMenuResponse menu) {
        Set<Long> productIds = new LinkedHashSet<>();
        for (ProposedWeekMenuDayResponse day : safeDays(menu)) {
            for (ProposedWeekMenuSectionResponse section : safeSections(day)) {
                for (ProposedWeekMenuProductResponse product : safeProducts(section)) {
                    if (product.productId() != null) {
                        productIds.add(product.productId());
                    }
                }
            }
        }
        for (CurrentWeekMenuRecipeProductionResponse production : safeProductions(menu)) {
            if (production.productId() != null) {
                productIds.add(production.productId());
            }
        }
        return productIds;
    }

    private static List<ProposedWeekMenuDay> safeDays(ProposedWeekMenu menu) {
        return menu.getDays() == null ? List.of() : menu.getDays();
    }

    private static List<ProposedWeekMenuSection> safeSections(ProposedWeekMenuDay day) {
        return day.getSections() == null ? List.of() : day.getSections();
    }

    private static List<ProposedWeekMenuProduct> safeProducts(ProposedWeekMenuSection section) {
        return section.getProducts() == null ? List.of() : section.getProducts();
    }

    private static List<ProposedWeekMenuRecipeProduction> safeProductions(ProposedWeekMenuDay day) {
        return day.getRecipeProductions() == null ? List.of() : day.getRecipeProductions();
    }

    private static List<ProposedWeekMenuDayResponse> safeDays(CurrentWeekMenuResponse menu) {
        return menu.days() == null ? List.of() : menu.days();
    }

    private static List<ProposedWeekMenuSectionResponse> safeSections(ProposedWeekMenuDayResponse day) {
        return day.sections() == null ? List.of() : day.sections();
    }

    private static List<ProposedWeekMenuProductResponse> safeProducts(ProposedWeekMenuSectionResponse section) {
        return section.products() == null ? List.of() : section.products();
    }

    private static List<CurrentWeekMenuRecipeProductionResponse> safeProductions(CurrentWeekMenuResponse menu) {
        return menu.recipeProductions() == null ? List.of() : menu.recipeProductions();
    }
}
