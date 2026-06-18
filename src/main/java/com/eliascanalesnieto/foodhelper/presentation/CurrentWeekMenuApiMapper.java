package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuShoppingListItem;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuUsedStock;
import org.springframework.stereotype.Component;

@Component
public class CurrentWeekMenuApiMapper {
    public CurrentWeekMenuResponse toResponse(CurrentWeekMenu menu) {
        return new CurrentWeekMenuResponse(
                menu.getId(),
                menu.getProposedWeekMenuId(),
                menu.getStartDate(),
                menu.getEndDate(),
                menu.getDays().stream().map(this::toResponse).toList(),
                toResponse(menu.getNutritionalValues()),
                toResponse(menu.getStockSummary()),
                menu.getUsedStock().stream().map(this::toResponse).toList(),
                menu.getShoppingList().stream().map(this::toResponse).toList()
        );
    }

    public CurrentWeekMenuUsedStockResponse toResponse(CurrentWeekMenuUsedStock usedStock) {
        return new CurrentWeekMenuUsedStockResponse(
                usedStock.getStockEntryId(),
                usedStock.getProductId(),
                usedStock.getProductName(),
                usedStock.getUsedUnits(),
                usedStock.getPrice(),
                usedStock.getTotalCost(),
                usedStock.getExpirationDate(),
                usedStock.getEntryDate()
        );
    }

    public CurrentWeekMenuShoppingListItemResponse toResponse(CurrentWeekMenuShoppingListItem shoppingListItem) {
        return new CurrentWeekMenuShoppingListItemResponse(
                shoppingListItem.getProductId(),
                shoppingListItem.getProductName(),
                shoppingListItem.getMissingUnits()
        );
    }

    public java.util.List<CurrentWeekMenuUsedStockResponse> toResponse(java.util.List<CurrentWeekMenuUsedStock> usedStock) {
        return usedStock.stream().map(this::toResponse).toList();
    }

    public java.util.List<CurrentWeekMenuShoppingListItemResponse> toResponseShoppingList(java.util.List<CurrentWeekMenuShoppingListItem> shoppingList) {
        return shoppingList.stream().map(this::toResponse).toList();
    }

    private ProposedWeekMenuDayResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay day) {
        return new ProposedWeekMenuDayResponse(
                day.getId(),
                day.getDate(),
                day.getSections().stream().map(this::toResponse).toList(),
                toResponse(day.getNutritionalValues())
        );
    }

    private ProposedWeekMenuSectionResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection section) {
        return new ProposedWeekMenuSectionResponse(
                section.getId(),
                section.getDayPartId(),
                section.getName(),
                section.getDescription(),
                section.getSortOrder(),
                section.getProducts().stream().map(this::toResponse).toList(),
                toResponse(section.getNutritionalValues())
        );
    }

    private ProposedWeekMenuProductResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct product) {
        return new ProposedWeekMenuProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getUnits(),
                product.getGrams(),
                product.getSortOrder(),
                toResponse(product.getNutritionalValues())
        );
    }

    private NutritionalValuesResponse toResponse(com.eliascanalesnieto.foodhelper.domain.NutritionalValues nutritionalValues) {
        return new NutritionalValuesResponse(
                nutritionalValues.getCalories(),
                nutritionalValues.getCarbohydrates(),
                nutritionalValues.getProteins(),
                nutritionalValues.getFats()
        );
    }

    private ProposedWeekMenuStockSummaryResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummary stockSummary) {
        if (stockSummary == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryResponse(
                stockSummary.getPlannedDays(),
                stockSummary.getDistinctProducts(),
                toResponse(stockSummary.getCalories()),
                stockSummary.getEstimatedCost(),
                stockSummary.getRequirements().stream().map(this::toResponse).toList()
        );
    }

    private ProposedWeekMenuStockSummaryCaloriesResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryCalories calories) {
        return new ProposedWeekMenuStockSummaryCaloriesResponse(
                calories.getAveragePerPlannedDay(),
                toResponse(calories.getMaxDay()),
                toResponse(calories.getMinDay())
        );
    }

    private ProposedWeekMenuStockSummaryDayCaloriesResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryDayCalories dayCalories) {
        if (dayCalories == null) {
            return null;
        }
        return new ProposedWeekMenuStockSummaryDayCaloriesResponse(
                dayCalories.getDate(),
                dayCalories.getCalories()
        );
    }

    private ProposedWeekMenuStockRequirementResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockRequirement requirement) {
        return new ProposedWeekMenuStockRequirementResponse(
                requirement.getProductId(),
                requirement.getProductName(),
                requirement.getRequiredUnits(),
                requirement.getAvailableUnits(),
                requirement.getCoveredUnits(),
                requirement.getMissingUnits(),
                requirement.getEstimatedCost()
        );
    }
}
