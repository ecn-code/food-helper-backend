package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "PlanningProductRequest", description = "Ordered product consumed in one planning section. Use productId for a catalog product or productName plus absolute nutritional values for a manual item.")
public record ProposedWeekMenuProductRequest(
        @Schema(description = "Product identifier for a catalog product. Leave null for a manual item.", example = "1", nullable = true)
        Long productId,
        @Schema(description = "Visible name for a manual item. Leave null for a catalog product.", example = "Homemade fruit bowl", nullable = true)
        String productName,
        @Schema(description = "Consumed units. Optional for catalog products. Must be null for manual items.", example = "2", nullable = true)
        @Positive BigDecimal units,
        @Schema(description = "Consumed grams. Optional for catalog products when overriding the default conversion. Must be null for manual items.", example = "300", nullable = true)
        @Positive BigDecimal grams,
        @Schema(description = "Manual calories for the full line. Required when productId is null.", example = "125", nullable = true)
        @PositiveOrZero BigDecimal calories,
        @Schema(description = "Manual carbohydrates for the full line. Required when productId is null.", example = "18", nullable = true)
        @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Manual proteins for the full line. Required when productId is null.", example = "4", nullable = true)
        @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Manual fats for the full line. Required when productId is null.", example = "2", nullable = true)
        @PositiveOrZero BigDecimal fats,
        @Schema(description = "Explicit display order within the section. Must be unique within each section.", example = "10")
        @NotNull @PositiveOrZero Integer sortOrder
) {
    public ProposedWeekMenuProductRequest(
            Long productId,
            BigDecimal units,
            BigDecimal grams,
            Integer sortOrder
    ) {
        this(productId, null, units, grams, null, null, null, null, sortOrder);
    }

    public ProposedWeekMenuProductRequest(
            String productName,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            Integer sortOrder
    ) {
        this(null, productName, null, null, calories, carbohydrates, proteins, fats, sortOrder);
    }
}
