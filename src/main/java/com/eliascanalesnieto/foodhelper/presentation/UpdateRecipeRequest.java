package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.math.BigDecimal;

@Schema(name = "UpdateRecipeRequest", description = "Payload for updating a recipe")
public record UpdateRecipeRequest(
        @Schema(description = "Recipe name", example = "Chicken curry")
        @NotBlank String name,
        @Schema(description = "Recipe description", example = "Balanced curry with chicken and vegetables")
        @NotBlank String description,
        @Schema(description = "Recipe preparation instructions", example = "Cook vegetables first, then finish the curry.")
        @NotBlank String instructions,
        @ArraySchema(schema = @Schema(implementation = RecipeIngredientAssignmentRequest.class),
                arraySchema = @Schema(description = "Assigned ingredient products with their quantity and quantity type"))
        @NotEmpty List<@Valid RecipeIngredientAssignmentRequest> products,
        @Schema(description = "Optional default number of units produced by the recipe", example = "4", nullable = true)
        @Positive BigDecimal defaultUnitsProduced,
        @Schema(description = "Optional flag to switch the derived product stock between component stock and self stock", example = "true", nullable = true)
        Boolean stockFromComposition,
        @Schema(description = "Optional recipe photo that will be compressed before storage")
        @Valid PhotoUploadRequest photo
) {
    public UpdateRecipeRequest(String name, String description, String instructions, List<@Valid RecipeIngredientAssignmentRequest> products) {
        this(name, description, instructions, products, null, null, null);
    }
}
