package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "CreateProductRequest", description = "Payload for creating a product")
public record CreateProductRequest(
        @Schema(description = "Product name", example = "Apple")
        @NotBlank String name,
        @Schema(description = "Product description", example = "Fresh apple")
        @NotBlank String description,
        @Schema(description = "Calories per serving", example = "52")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohydrates per serving", example = "14")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteins per serving", example = "0.3")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Fats per serving", example = "0.2")
        @NotNull @PositiveOrZero BigDecimal fats
) {
}
