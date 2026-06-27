package com.eliascanalesnieto.foodhelper.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CurrentWeekMenu {
    Long id;
    Long proposedWeekMenuId;
    Long payerUserId;
    String payerUsername;
    LocalDate startDate;
    LocalDate endDate;
    List<ProposedWeekMenuDay> days;
    NutritionalValues nutritionalValues;
    ProposedWeekMenuStockSummary stockSummary;
    List<CurrentWeekMenuUsedStock> usedStock;
    List<CurrentWeekMenuShoppingListItem> shoppingList;
}
