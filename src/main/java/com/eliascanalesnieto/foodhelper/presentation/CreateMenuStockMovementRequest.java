package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(name = "CreateMenuStockMovementRequest", description = "Repercussion movement recorded for an open menu")
public record CreateMenuStockMovementRequest(
        @Schema(description = "Optional user that will be charged. When omitted, the menu responsible is used as default.", example = "2", nullable = true)
        Long userId,
        @NotNull
        @Schema(description = "Product identifier affected by the purchase or adjustment", example = "11")
        Long productId,
        @NotNull
        @DecimalMin(value = "0.01")
        @Schema(description = "Quantity added to the menu stock", example = "1.25")
        BigDecimal quantity,
        @NotNull
        @DecimalMin(value = "0.00")
        @Schema(description = "Price paid for one unit", example = "2.49")
        BigDecimal price,
        @Schema(description = "Optional description for the repercussion", example = "Weekly groceries")
        String description
) {
}
