package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "MenuItemImportsResponse", description = "Complete result of an atomic menu item import")
public record MenuItemImportsResponse(
        @Schema(description = "Updated menu including imported temporary stock lines and recalculated shopping list")
        CurrentWeekMenuResponse menu,
        @Schema(description = "Money box movements created by MONEY_BOX rows, in request order")
        List<MoneyBoxMovementResponse> moneyBoxMovements,
        @Schema(description = "Global stock batches created by GLOBAL_STOCK rows, in request order")
        List<StockEntryResponse> globalStockEntries
) {
    public MenuItemImportsResponse {
        moneyBoxMovements = moneyBoxMovements == null ? List.of() : List.copyOf(moneyBoxMovements);
        globalStockEntries = globalStockEntries == null ? List.of() : List.copyOf(globalStockEntries);
    }
}
