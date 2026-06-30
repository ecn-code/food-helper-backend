package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "MenuStockMovementResponse", description = "Historical stock repercussion movement recorded for an open menu")
public record MenuStockMovementResponse(
        @Schema(description = "Movement identifier", example = "1")
        Long id,
        @Schema(description = "Menu identifier", example = "7")
        Long menuId,
        @Schema(description = "User identifier charged for the movement", example = "2")
        Long userId,
        @Schema(description = "Username charged for the movement", example = "elias")
        String userUsername,
        @Schema(description = "Product identifier affected", example = "11")
        Long productId,
        @Schema(description = "Product name affected", example = "Rice")
        String productName,
        @Schema(description = "Quantity repercuted into menu stock", example = "1.25")
        BigDecimal quantity,
        @Schema(description = "Unit price", example = "2.49")
        BigDecimal price,
        @Schema(description = "Total cost of the movement", example = "3.11")
        BigDecimal totalCost,
        @Schema(description = "Optional description", nullable = true)
        String description,
        @Schema(description = "UTC timestamp for the movement")
        LocalDateTime createdAt
) {
}
