package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "MenuShoppingListItemResponse", description = "Missing product units that should be bought for a menu")
public record CurrentWeekMenuShoppingListItemResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Apple")
        String productName,
        @Schema(description = "Units still missing after consuming available stock", example = "2.00")
        BigDecimal missingUnits
) {
}
