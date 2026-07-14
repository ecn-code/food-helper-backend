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
    private final WeekMenuSnapshotMapper snapshotMapper;

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
                snapshotMapper.toResponseDays(days),
                snapshotMapper.toResponse(menu.getNutritionalValues()),
                snapshotMapper.toResponse(menu.getStockSummary()),
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

}
