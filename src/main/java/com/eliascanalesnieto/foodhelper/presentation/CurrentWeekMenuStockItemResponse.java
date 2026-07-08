package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "WeekStockItemResponse", description = "Temporary stock line tracked for the established week with quantity and unit price. The same product may appear more than once.")
public record CurrentWeekMenuStockItemResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Rice")
        String productName,
        @Schema(description = "Quantity currently available in the menu stock", example = "1.50")
        BigDecimal quantity,
        @Schema(description = "Unit price stored for the menu stock item", example = "2.49")
        BigDecimal price
) {
}
