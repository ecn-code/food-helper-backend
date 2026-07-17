package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "PlanningStockRequirementResponse", description = "Per-product stock preview for a planning")
public record ProposedWeekMenuStockRequirementResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Apple")
        String productName,
        @Schema(description = "Whether all quantity fields in this requirement are expressed in units instead of grams", example = "false")
        boolean isStockInUnits,
        @Schema(description = "Total quantity required by the planning, expressed in units when isStockInUnits is true or grams otherwise", example = "4.50")
        BigDecimal requiredUnits,
        @Schema(description = "Total quantity available in stock, expressed in units when isStockInUnits is true or grams otherwise", example = "6.00")
        BigDecimal availableUnits,
        @Schema(description = "Quantity that can be covered by available stock, expressed in units when isStockInUnits is true or grams otherwise", example = "4.50")
        BigDecimal coveredUnits,
        @Schema(description = "Quantity still missing after consuming all available stock, expressed in units when isStockInUnits is true or grams otherwise", example = "0.00")
        BigDecimal missingUnits,
        @Schema(description = "Estimated cost for the covered units", example = "12.75")
        BigDecimal estimatedCost
) {
}
