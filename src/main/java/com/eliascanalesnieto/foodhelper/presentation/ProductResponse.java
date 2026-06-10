package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductResponse", description = "API representation of a product")
public record ProductResponse(
        @Schema(description = "Product identifier", example = "1")
        Long id,
        @Schema(description = "Product name", example = "Apple")
        String name,
        @Schema(description = "Product description", example = "Fresh apple")
        String description,
        @Schema(description = "Product nutritional values")
        NutritionalValuesResponse nutritionalValues
) {
}
