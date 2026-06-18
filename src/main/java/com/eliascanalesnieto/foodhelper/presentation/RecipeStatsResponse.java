package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "RecipeStatsResponse", description = "Aggregated recipe statistics calculated on the server")
public record RecipeStatsResponse(
        @Schema(description = "Number of active recipes", example = "4")
        long activeRecipes,
        @Schema(description = "Average recipe calories", example = "512.50")
        BigDecimal averageCalories,
        @Schema(description = "Total number of ingredients across all recipes", example = "11")
        long totalIngredients,
        @Schema(description = "Number of recipes with a derived product", example = "2")
        long recipesWithDerivedProduct
) {
}
