package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "CreateProductRequest", description = "Payload for creating a product")
public record CreateProductRequest(
        @Schema(description = "Product name", example = "Apple")
        @NotBlank String name,
        @Schema(description = "Product description", example = "Fresh apple")
        @NotBlank String description,
        @Schema(description = "Default grams represented by one unit of this product", example = "150")
        @NotNull @Positive BigDecimal gramsPerUnit,
        @Schema(description = "Calories per 100 grams", example = "52")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohydrates per 100 grams", example = "14")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteins per 100 grams", example = "0.3")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Fats per 100 grams", example = "0.2")
        @NotNull @PositiveOrZero BigDecimal fats
) {
}
