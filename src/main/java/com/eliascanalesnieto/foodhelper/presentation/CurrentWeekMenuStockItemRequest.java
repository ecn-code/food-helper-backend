package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "WeekStockItemRequest", description = "Temporary stock item to save on the established week")
public record CurrentWeekMenuStockItemRequest(
        @Schema(description = "Product identifier", example = "1")
        @NotNull Long productId,
        @Schema(description = "Quantity available in the menu stock", example = "1.50")
        @NotNull @Positive BigDecimal quantity
) {
}
