package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SupermarketResponse", description = "API representation of a supermarket")
public record SupermarketResponse(
        @Schema(description = "Supermarket identifier", example = "1") Long id,
        @Schema(description = "Supermarket name", example = "Mercadona") String name
) {
}
