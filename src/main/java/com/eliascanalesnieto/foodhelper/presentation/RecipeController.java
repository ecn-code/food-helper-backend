package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.PageResult;
import com.eliascanalesnieto.foodhelper.application.PaginationRequest;
import com.eliascanalesnieto.foodhelper.application.RecipeService;
import com.eliascanalesnieto.foodhelper.application.StatsService;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeSearchCriteria;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "List, create, update, delete, derive product, and inspect recipe statistics")
public class RecipeController {
    private final RecipeService service;
    private final StatsService statsService;
    private final ProductApiMapper mapper;

    @GetMapping
    @Operation(
            summary = "List recipes",
            description = "Returns recipes ordered alphabetically by name, with identifier as a stable tiebreaker. Text search ignores case and accents across name, description, instructions, and ingredients; nutritional ranges apply to calculated recipe totals."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipes returned",
                    content = @Content(schema = @Schema(implementation = RecipePageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or nutritional range",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public RecipePageResponse findAll(
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0")
            int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page, between 1 and 100", example = "20")
            @RequestParam(defaultValue = "20")
            int size,
            @io.swagger.v3.oas.annotations.Parameter(description = "Accent- and case-insensitive text", example = "chicken curry")
            @RequestParam(required = false) String search,
            @io.swagger.v3.oas.annotations.Parameter(description = "Minimum calculated calories, inclusive", example = "300")
            @RequestParam(required = false) BigDecimal caloriesMin,
            @io.swagger.v3.oas.annotations.Parameter(description = "Maximum calculated calories, inclusive", example = "700")
            @RequestParam(required = false) BigDecimal caloriesMax,
            @io.swagger.v3.oas.annotations.Parameter(description = "Minimum calculated carbohydrates, inclusive", example = "20")
            @RequestParam(required = false) BigDecimal carbohydratesMin,
            @io.swagger.v3.oas.annotations.Parameter(description = "Maximum calculated carbohydrates, inclusive", example = "80")
            @RequestParam(required = false) BigDecimal carbohydratesMax,
            @io.swagger.v3.oas.annotations.Parameter(description = "Minimum calculated proteins, inclusive", example = "15")
            @RequestParam(required = false) BigDecimal proteinsMin,
            @io.swagger.v3.oas.annotations.Parameter(description = "Maximum calculated proteins, inclusive", example = "60")
            @RequestParam(required = false) BigDecimal proteinsMax,
            @io.swagger.v3.oas.annotations.Parameter(description = "Minimum calculated fats, inclusive", example = "5")
            @RequestParam(required = false) BigDecimal fatsMin,
            @io.swagger.v3.oas.annotations.Parameter(description = "Maximum calculated fats, inclusive", example = "30")
            @RequestParam(required = false) BigDecimal fatsMax,
            @io.swagger.v3.oas.annotations.Parameter(description = "Whether the recipe must have a derived product", example = "true")
            @RequestParam(required = false) Boolean hasDerivedProduct
    ) {
        RecipeSearchCriteria criteria = new RecipeSearchCriteria(
                search, caloriesMin, caloriesMax, carbohydratesMin, carbohydratesMax,
                proteinsMin, proteinsMax, fatsMin, fatsMax, hasDerivedProduct
        );
        PageResult<com.eliascanalesnieto.foodhelper.domain.Recipe> result = service.findPage(
                PaginationRequest.of(page, size), criteria
        );
        return new RecipePageResponse(result.items().stream()
                .map(mapper::toResponse)
                .toList(), result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Recipe statistics",
            description = "Returns aggregated recipe metrics calculated on the server from the current database state."
    )
    @ApiResponse(responseCode = "200", description = "Recipe statistics returned",
            content = @Content(schema = @Schema(implementation = RecipeStatsResponse.class)))
    public RecipeStatsResponse stats() {
        return statsService.getRecipeStats();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create recipe",
            description = "Creates a recipe from existing products and calculates its nutritional totals from assigned quantities."
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
                request.defaultUnitsProduced(),
                toDomainIngredients(request.products()),
                request.photo() == null ? null : request.photo().toDomain()
        ));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update recipe",
            description = "Updates an existing recipe, recalculates its nutritional totals, and synchronizes its derived product if it exists. If a stock mode is provided, it also switches the derived product between composition stock and self stock."
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
                request.defaultUnitsProduced(),
                request.stockFromComposition(),
                toDomainIngredients(request.products()),
                request.photo() == null ? null : request.photo().toDomain()
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
            description = "Creates a product from a recipe once, lets the caller choose the derived product name and stock mode, stores the yield metadata, and keeps the derived product synchronized with future recipe changes."
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
        return mapper.toResponse(service.createDerivedProduct(id, request.name(), request.units(), request.stockFromComposition()));
    }

    private List<RecipeIngredient> toDomainIngredients(List<RecipeIngredientAssignmentRequest> products) {
        return products.stream()
                .map(product -> RecipeIngredient.builder()
                        .productId(product.productId())
                        .quantity(product.quantity())
                        .quantityType(product.quantityType())
                        .build())
                .toList();
    }
}
