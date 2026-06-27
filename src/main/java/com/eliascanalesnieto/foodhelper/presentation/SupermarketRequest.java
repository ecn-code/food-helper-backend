package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SupermarketRequest", description = "Payload for creating or updating a supermarket")
public record SupermarketRequest(
        @Schema(description = "Unique supermarket name", example = "Mercadona")
        @NotBlank @Size(max = 150) String name
) {
}
