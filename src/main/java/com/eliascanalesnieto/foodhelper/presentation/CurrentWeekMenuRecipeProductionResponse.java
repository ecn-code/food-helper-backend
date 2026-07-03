package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "MenuRecipeProductionResponse", description = "Recipe production stored in a menu with its transfer trace to product stock")
public record CurrentWeekMenuRecipeProductionResponse(
        @Schema(description = "Recipe production identifier", example = "1")
        Long id,
        @Schema(description = "Recipe identifier", example = "12")
        Long recipeId,
        @Schema(description = "Recipe name", example = "Chicken curry")
        String recipeName,
        @Schema(description = "Product identifier created from the recipe", example = "22")
        Long productId,
        @Schema(description = "Product name created from the recipe", example = "Chicken curry")
        String productName,
        @Schema(description = "Produced units that can become stock", example = "4.00")
        BigDecimal units,
        @Schema(description = "Explicit display order within the day", example = "10")
        Integer sortOrder,
        @Schema(description = "Whether this production has already been transferred to stock")
        boolean transferred,
        @Schema(description = "Transfer mode used for the stock entry", example = "MANUAL", nullable = true)
        String transferType,
        @Schema(description = "Stock entry created from this production", example = "88", nullable = true)
        Long stockEntryId,
        @Schema(description = "Date and time when the transfer happened", nullable = true)
        LocalDateTime transferredAt
) {
}
