package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;

@Schema(name = "ProductResponse", description = "API representation of a product")
public record ProductResponse(
        @Schema(description = "Product identifier", example = "1")
        Long id,
        @Schema(description = "Product name", example = "Apple")
        String name,
        @Schema(description = "Product description", example = "Fresh apple")
        String description,
        @Schema(description = "Default grams represented by one unit of this product", example = "150")
        BigDecimal gramsPerUnit,
        @Schema(description = "Whether stock quantities are returned in units instead of stored grams", example = "false")
        boolean isStockInUnits,
        @Schema(description = "Nutrition basis used by this product", example = "PER_100_GRAMS")
        NutritionBasis nutritionBasis,
        @Schema(description = "Optional default price for this product. Supports up to four decimal places", example = "0.0068", nullable = true)
        BigDecimal defaultPrice,
        @Schema(description = "Product nutritional values")
        NutritionalValuesResponse nutritionalValues,
        @Schema(description = "Optional signed photo URL that expires with the authentication token lifetime", example = "/api/v1/media/12?expiresAt=1781611200&signature=5f2a...")
        String photo,
        @Schema(description = "Supermarkets where the product is available")
        List<SupermarketResponse> supermarkets,
        @Schema(description = "Derived recipe information when this product comes from a recipe", nullable = true)
        RecipeDerivedProductResponse derivedProduct
) {
}
