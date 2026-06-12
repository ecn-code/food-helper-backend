package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ProposedWeekMenuProductResponse", description = "API representation of one ordered proposed menu product")
public record ProposedWeekMenuProductResponse(
        @Schema(description = "Product identifier", example = "1")
        Long productId,
        @Schema(description = "Product name", example = "Apple")
        String productName,
        @Schema(description = "Consumed units", example = "2")
        BigDecimal units,
        @Schema(description = "Consumed grams", example = "300")
        BigDecimal grams,
        @Schema(description = "Explicit display order within the section", example = "10")
        Integer sortOrder,
        @Schema(description = "Nutritional contribution for this consumed product")
        NutritionalValuesResponse nutritionalValues
) {
}
