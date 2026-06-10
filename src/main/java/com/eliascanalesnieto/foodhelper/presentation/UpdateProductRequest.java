package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "UpdateProductRequest", description = "Payload for updating a product")
public record UpdateProductRequest(
        @Schema(description = "Product name", example = "Green Apple")
        @NotBlank String name,
        @Schema(description = "Product description", example = "Green apple")
        @NotBlank String description,
        @Schema(description = "Calories per serving", example = "48")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohydrates per serving", example = "13")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteins per serving", example = "0.4")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Fats per serving", example = "0.1")
        @NotNull @PositiveOrZero BigDecimal fats
) {
}
