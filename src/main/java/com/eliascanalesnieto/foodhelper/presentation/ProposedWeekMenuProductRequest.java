package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "ProposedWeekMenuProductRequest", description = "Ordered product consumed in one proposed menu section")
public record ProposedWeekMenuProductRequest(
        @Schema(description = "Product identifier", example = "1")
        @NotNull Long productId,
        @Schema(description = "Consumed units. Defaults to 1 when omitted.", example = "2")
        @Positive BigDecimal units,
        @Schema(description = "Consumed grams. Defaults to product gramsPerUnit multiplied by units when omitted.", example = "300")
        @Positive BigDecimal grams,
        @Schema(description = "Explicit display order within the section", example = "10")
        @NotNull @PositiveOrZero Integer sortOrder
) {
}
