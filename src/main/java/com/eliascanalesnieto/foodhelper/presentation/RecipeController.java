package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.RecipeService;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "Create, update, delete, and derive product operations for recipes")
public class RecipeController {
    private final RecipeService service;
    private final ProductApiMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create recipe",
            description = "Creates a recipe from existing products and calculates its nutritional totals based on assigned grams."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recipe created",
                    content = @Content(schema = @Schema(implementation = RecipeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "One or more ingredient products were not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict while creating the recipe",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public RecipeResponse create(@Valid @RequestBody CreateRecipeRequest request) {
        return mapper.toResponse(service.create(
                request.name(),
                request.description(),
                request.instructions(),
                toDomainIngredients(request.products())
        ));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update recipe",
            description = "Updates an existing recipe, recalculates its nutritional totals, and synchronizes its derived product if it exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe updated",
                    content = @Content(schema = @Schema(implementation = RecipeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Recipe or one of its ingredient products was not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict while updating the recipe",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public RecipeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRecipeRequest request) {
        return mapper.toResponse(service.update(
                id,
                request.name(),
                request.description(),
                request.instructions(),
                toDomainIngredients(request.products())
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete recipe",
            description = "Deletes a recipe and its derived product link. If the recipe has already created a derived product, that product is deleted as well."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recipe deleted"),
            @ApiResponse(responseCode = "404", description = "Recipe not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/derived-product")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create derived product from recipe",
            description = "Creates a product from a recipe once, stores the yield metadata, and keeps the derived product synchronized with future recipe changes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Derived product created",
                    content = @Content(schema = @Schema(implementation = RecipeDerivedProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Recipe not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Derived product already exists or product name conflicts",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public RecipeDerivedProductResponse createDerivedProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateRecipeDerivedProductRequest request
    ) {
        return mapper.toResponse(service.createDerivedProduct(id, request.producedGrams(), request.gramsPerUnit()));
    }

    private List<RecipeIngredient> toDomainIngredients(List<RecipeIngredientAssignmentRequest> products) {
        return products.stream()
                .map(product -> RecipeIngredient.builder()
                        .productId(product.productId())
                        .grams(product.grams())
                        .build())
                .toList();
    }
}
