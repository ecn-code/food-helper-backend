package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "UpdateProductRequest", description = "Payload for updating a product")
public record UpdateProductRequest(
        @Schema(description = "Product name", example = "Green Apple")
        @NotBlank String name,
        @Schema(description = "Product description", example = "Green apple")
        @NotBlank String description,
        @Schema(description = "Default grams represented by one unit of this product", example = "140")
        @NotNull @Positive BigDecimal gramsPerUnit,
        @Schema(description = "Calories per 100 grams", example = "48")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohydrates per 100 grams", example = "13")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteins per 100 grams", example = "0.4")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Fats per 100 grams", example = "0.1")
        @NotNull @PositiveOrZero BigDecimal fats
) {
}
