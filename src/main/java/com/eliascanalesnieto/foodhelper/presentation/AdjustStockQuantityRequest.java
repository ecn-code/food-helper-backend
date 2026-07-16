package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "AdjustStockQuantityRequest", description = "Payload for increasing or decreasing stock quantity")
public record AdjustStockQuantityRequest(
        @Schema(description = "Quantity in grams to add or remove", example = "50")
        @NotNull @Positive BigDecimal quantity
) {
}
