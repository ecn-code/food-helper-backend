package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "CurrentWeekMenuShoppingListItemResponse", description = "Missing product units that should be bought for an established week menu")
public record CurrentWeekMenuShoppingListItemResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Apple")
        String productName,
        @Schema(description = "Units still missing after consuming available stock", example = "2.00")
        BigDecimal missingUnits
) {
}
