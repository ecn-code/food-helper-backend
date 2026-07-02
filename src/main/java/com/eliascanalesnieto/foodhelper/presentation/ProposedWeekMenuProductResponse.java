package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "PlanningProductResponse", description = "API representation of one ordered planning product")
public record ProposedWeekMenuProductResponse(
        @Schema(description = "Product identifier for a catalog product. Null for a manual item.", example = "1", nullable = true)
        Long productId,
        @Schema(description = "Product name or manual label", example = "Apple")
        String productName,
        @Schema(description = "Consumed units. Null for manual items.", example = "2", nullable = true)
        BigDecimal units,
        @Schema(description = "Consumed grams. Null for manual items.", example = "300", nullable = true)
        BigDecimal grams,
        @Schema(description = "Explicit display order within the section. Values are unique within the section.", example = "10")
        Integer sortOrder,
        @Schema(description = "Nutritional contribution for this consumed product")
        NutritionalValuesResponse nutritionalValues
) {
}
