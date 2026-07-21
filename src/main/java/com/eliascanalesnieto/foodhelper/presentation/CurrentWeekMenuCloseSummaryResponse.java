package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "CloseMenuSummaryResponse", description = "Preview shown before closing a menu")
public record CurrentWeekMenuCloseSummaryResponse(
        @Schema(description = "Menu identifier", example = "1")
        Long menuId,
        @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuStockItemResponse.class))
        List<CurrentWeekMenuStockItemResponse> transferableWeekStock,
        @ArraySchema(schema = @Schema(implementation = PositiveStockProductResponse.class))
        List<PositiveStockProductResponse> positiveStockProducts,
        @Schema(description = "Estimated value of the week stock that would be transferred to global stock", example = "12.30")
        BigDecimal transferableWeekStockValue,
        @ArraySchema(schema = @Schema(implementation = UserMoneyMovementResponse.class))
        List<UserMoneyMovementResponse> moneyMovements,
        @Schema(description = "Total coupons credited to the user's money box for this menu", example = "15.00")
        BigDecimal couponRewards,
        @Schema(description = "Total menu expense charged to the user's money box", example = "9.80")
        BigDecimal menuExpense,
        @Schema(description = "Coupons minus expense for the menu", example = "5.20")
        BigDecimal netMoneyImpact
) {
    public CurrentWeekMenuCloseSummaryResponse {
        transferableWeekStock = transferableWeekStock == null ? List.of() : List.copyOf(transferableWeekStock);
        positiveStockProducts = positiveStockProducts == null ? List.of() : List.copyOf(positiveStockProducts);
        moneyMovements = moneyMovements == null ? List.of() : List.copyOf(moneyMovements);
    }

    public CurrentWeekMenuCloseSummaryResponse(Long menuId, List<CurrentWeekMenuStockItemResponse> transferableWeekStock,
            BigDecimal transferableWeekStockValue, List<UserMoneyMovementResponse> moneyMovements,
            BigDecimal couponRewards, BigDecimal menuExpense, BigDecimal netMoneyImpact) {
        this(menuId, transferableWeekStock, List.of(), transferableWeekStockValue, moneyMovements,
                couponRewards, menuExpense, netMoneyImpact);
    }
}
