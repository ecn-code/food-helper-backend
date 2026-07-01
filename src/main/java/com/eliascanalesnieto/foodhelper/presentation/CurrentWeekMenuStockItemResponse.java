package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "WeekStockItemResponse", description = "Temporary stock item tracked for the established week")
public record CurrentWeekMenuStockItemResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Rice")
        String productName,
        @Schema(description = "Quantity currently available in the menu stock", example = "1.50")
        BigDecimal quantity
) {
}
