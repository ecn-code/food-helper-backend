package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductResponse", description = "Representacion API de un producto")
public record ProductResponse(
        @Schema(description = "Identificador del producto", example = "1")
        Long id,
        @Schema(description = "Nombre del producto", example = "Apple")
        String name,
        @Schema(description = "Descripcion del producto", example = "Fresh apple")
        String description,
        @Schema(description = "Valores nutricionales del producto")
        NutritionalValuesResponse nutritionalValues
) {
}
