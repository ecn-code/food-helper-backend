package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "WeekStockItemRequest", description = "Temporary stock line with quantity and unit price saved on the established week. The same product may appear more than once.")
public record CurrentWeekMenuStockItemRequest(
        @Schema(description = "Product identifier", example = "1")
        @NotNull Long productId,
        @Schema(description = "Quantity available in the menu stock", example = "1.50")
        @NotNull @Positive BigDecimal quantity,
        @Schema(description = "Unit price stored for the menu stock item", example = "2.49")
        @NotNull @DecimalMin(value = "0.00") BigDecimal price
) {
}
