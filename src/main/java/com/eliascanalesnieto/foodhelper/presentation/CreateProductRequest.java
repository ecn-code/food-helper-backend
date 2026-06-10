package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "CreateProductRequest", description = "Payload para crear un producto")
public record CreateProductRequest(
        @Schema(description = "Nombre del producto", example = "Apple")
        @NotBlank String name,
        @Schema(description = "Descripcion del producto", example = "Fresh apple")
        @NotBlank String description,
        @Schema(description = "Calorias por porcion", example = "52")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohidratos por porcion", example = "14")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteinas por porcion", example = "0.3")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Grasas por porcion", example = "0.2")
        @NotNull @PositiveOrZero BigDecimal fats
) {
}
