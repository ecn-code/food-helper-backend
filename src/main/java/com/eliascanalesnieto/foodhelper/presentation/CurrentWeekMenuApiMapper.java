package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuShoppingListItem;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStockItem;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuUsedStock;
import com.eliascanalesnieto.foodhelper.domain.MenuStockMovement;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentWeekMenuApiMapper {
    private final NutritionalRulesService nutritionalRulesService;

    public CurrentWeekMenuResponse toResponse(CurrentWeekMenu menu) {
        java.util.List<com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay> days =
                menu.getDays() == null ? java.util.List.of() : menu.getDays();
        return new CurrentWeekMenuResponse(
                menu.getId(),
                menu.getProposedWeekMenuId(),
                menu.getPayerUserId(),
                menu.getPayerUsername(),
                menu.getPersonIds(),
                menu.getStartDate(),
                menu.getEndDate(),
                days.stream().map(this::toResponse).toList(),
                toResponse(menu.getNutritionalValues()),
                toResponse(menu.getStockSummary()),
                (menu.getUsedStock() == null ? java.util.List.<CurrentWeekMenuUsedStock>of() : menu.getUsedStock()).stream().map(this::toResponse).toList(),
                (menu.getWeekStock() == null ? java.util.List.<CurrentWeekMenuStockItem>of() : menu.getWeekStock()).stream().map(this::toResponse).toList(),
                (menu.getShoppingList() == null ? java.util.List.<CurrentWeekMenuShoppingListItem>of() : menu.getShoppingList()).stream().map(this::toResponse).toList(),
                (menu.getStockMovements() == null ? java.util.List.<MenuStockMovement>of() : menu.getStockMovements()).stream().map(this::toResponse).toList(),
                (menu.getRecipeProductions() == null ? java.util.List.<CurrentWeekMenuRecipeProduction>of() : menu.getRecipeProductions()).stream().map(this::toResponse).toList(),
                nutritionalRulesService.evaluate(menu.getNutritionalValues(), days.size()),
                CurrentWeekMenuState.ESTABLISHED
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

    public java.util.List<CurrentWeekMenuStockItemResponse> toResponseWeekStock(java.util.List<CurrentWeekMenuStockItem> weekStock) {
        return weekStock.stream().map(this::toResponse).toList();
    }

    public java.util.List<MenuStockMovementResponse> toResponseStockMovements(java.util.List<MenuStockMovement> movements) {
        return movements.stream().map(this::toResponse).toList();
    }

    public java.util.List<CurrentWeekMenuRecipeProductionResponse> toResponseRecipeProductions(java.util.List<CurrentWeekMenuRecipeProduction> productions) {
        return productions.stream().map(this::toResponse).toList();
    }

    private ProposedWeekMenuDayResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay day) {
        return new ProposedWeekMenuDayResponse(
                day.getId(),
                day.getDate(),
                (day.getSections() == null ? java.util.List.<com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection>of() : day.getSections()).stream().map(this::toResponse).toList(),
                (day.getRecipeProductions() == null ? java.util.List.<com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction>of() : day.getRecipeProductions()).stream().map(this::toResponse).toList(),
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
                product.getProductId() == null ? null : product.getUnits(),
                product.getProductId() == null ? null : product.getGrams(),
                product.getSortOrder(),
                toResponse(product.getNutritionalValues())
        );
    }

    private ProposedWeekMenuRecipeProductionResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction production) {
        return new ProposedWeekMenuRecipeProductionResponse(
                production.getId(),
                production.getRecipeId(),
                production.getRecipeName(),
                production.getProductId(),
                production.getProductName(),
                production.getUnits(),
                production.getSortOrder()
        );
    }

    private CurrentWeekMenuRecipeProductionResponse toResponse(CurrentWeekMenuRecipeProduction production) {
        return new CurrentWeekMenuRecipeProductionResponse(
                production.getId(),
                production.getRecipeId(),
                production.getRecipeName(),
                production.getProductId(),
                production.getProductName(),
                production.getUnits(),
                production.getSortOrder(),
                production.isTransferred(),
                production.getTransferType(),
                production.getStockEntryId(),
                production.getTransferredAt()
        );
    }

    private CurrentWeekMenuStockItemResponse toResponse(CurrentWeekMenuStockItem weekStockItem) {
        return new CurrentWeekMenuStockItemResponse(
                weekStockItem.getProductId(),
                weekStockItem.getProductName(),
                weekStockItem.getQuantity(),
                weekStockItem.getPrice()
        );
    }

    private MenuStockMovementResponse toResponse(MenuStockMovement movement) {
        return new MenuStockMovementResponse(
                movement.getId(),
                movement.getCurrentWeekMenuId(),
                movement.getUserId(),
                movement.getUserUsername(),
                movement.getProductId(),
                movement.getProductName(),
                movement.getQuantity(),
                movement.getPrice(),
                movement.getTotalCost(),
                movement.getDescription(),
                movement.getCreatedAt()
        );
    }

    private NutritionalValuesResponse toResponse(com.eliascanalesnieto.foodhelper.domain.NutritionalValues nutritionalValues) {
        if (nutritionalValues == null) {
            return null;
        }
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
