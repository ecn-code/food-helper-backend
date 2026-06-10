package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "UpdateProductRequest", description = "Payload para actualizar un producto")
public record UpdateProductRequest(
        @Schema(description = "Nombre del producto", example = "Green Apple")
        @NotBlank String name,
        @Schema(description = "Descripcion del producto", example = "Green apple")
        @NotBlank String description,
        @Schema(description = "Calorias por porcion", example = "48")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohidratos por porcion", example = "13")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteinas por porcion", example = "0.4")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Grasas por porcion", example = "0.1")
        @NotNull @PositiveOrZero BigDecimal fats
) {
}
