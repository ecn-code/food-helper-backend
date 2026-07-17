package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "CreateProductRequest", description = "Payload for creating a product")
public record CreateProductRequest(
        @Schema(description = "Product name", example = "Apple")
        @NotBlank String name,
        @Schema(description = "Product description", example = "Fresh apple")
        @NotBlank String description,
        @Schema(description = "Default grams represented by one unit of this product", example = "150")
        @NotNull @Positive BigDecimal gramsPerUnit,
        @Schema(description = "Whether stock quantities must be returned in units instead of their stored grams", example = "false", defaultValue = "false")
        Boolean isStockInUnits,
        @Schema(description = "Calories per 100 grams", example = "52")
        @NotNull @PositiveOrZero BigDecimal calories,
        @Schema(description = "Carbohydrates per 100 grams", example = "14")
        @NotNull @PositiveOrZero BigDecimal carbohydrates,
        @Schema(description = "Proteins per 100 grams", example = "0.3")
        @NotNull @PositiveOrZero BigDecimal proteins,
        @Schema(description = "Fats per 100 grams", example = "0.2")
        @NotNull @PositiveOrZero BigDecimal fats,
        @Schema(description = "Optional default price for this product. Supports up to four decimal places", example = "0.0068", nullable = true)
        @PositiveOrZero BigDecimal defaultPrice,
        @Schema(description = "Optional product photo that will be compressed before storage")
        @Valid PhotoUploadRequest photo,
        @Schema(description = "Supermarket identifiers where the product is available", example = "[1, 2]")
        List<Long> supermarketIds
) {
    public CreateProductRequest(
            String name,
            String description,
            BigDecimal gramsPerUnit,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats
    ) {
        this(name, description, gramsPerUnit, false, calories, carbohydrates, proteins, fats, null, null, List.of());
    }

    public CreateProductRequest(
            String name,
            String description,
            BigDecimal gramsPerUnit,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            PhotoUploadRequest photo
    ) {
        this(name, description, gramsPerUnit, false, calories, carbohydrates, proteins, fats, null, photo, List.of());
    }

    public CreateProductRequest(
            String name,
            String description,
            BigDecimal gramsPerUnit,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            BigDecimal defaultPrice
    ) {
        this(name, description, gramsPerUnit, false, calories, carbohydrates, proteins, fats, defaultPrice, null, List.of());
    }

    public CreateProductRequest(
            String name,
            String description,
            BigDecimal gramsPerUnit,
            BigDecimal calories,
            BigDecimal carbohydrates,
            BigDecimal proteins,
            BigDecimal fats,
            BigDecimal defaultPrice,
            List<Long> supermarketIds
    ) {
        this(name, description, gramsPerUnit, false, calories, carbohydrates, proteins, fats, defaultPrice, null, supermarketIds);
    }
}
