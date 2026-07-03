package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "CreateRecipeDerivedProductRequest", description = "Payload for creating a product derived from a recipe")
public record CreateRecipeDerivedProductRequest(
        @Schema(description = "Name for the derived product", example = "Curry base")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "Number of units produced by the recipe", example = "4")
        @NotNull @Positive BigDecimal units,
        @Schema(description = "Whether stock should be expanded into the recipe ingredients instead of tracked as this product itself", example = "true")
        @NotNull Boolean stockFromComposition
) {
    public CreateRecipeDerivedProductRequest(String name, BigDecimal units) {
        this(name, units, Boolean.TRUE);
    }
}
