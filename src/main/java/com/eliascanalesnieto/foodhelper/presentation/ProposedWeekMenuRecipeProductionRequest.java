package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(name = "PlanningRecipeProductionRequest", description = "Recipe production scheduled for one planned day. Recipes do not count as meals and instead generate product stock.")
public record ProposedWeekMenuRecipeProductionRequest(
        @Schema(description = "Recipe identifier", example = "12")
        @NotNull Long recipeId,
        @Schema(description = "Produced grams that should become product stock", example = "400")
        @NotNull @Positive BigDecimal producedGrams,
        @Schema(description = "Explicit display order within the day. Must be unique among recipe productions in the same day.", example = "10")
        @NotNull @PositiveOrZero Integer sortOrder
) {
}
