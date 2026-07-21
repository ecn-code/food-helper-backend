package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.PageResult;
import com.eliascanalesnieto.foodhelper.application.PaginationRequest;
import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.application.StatsService;
import com.eliascanalesnieto.foodhelper.domain.ProductSearchCriteria;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Create, update, delete, and inspect product statistics")
public class ProductController {
    private final ProductService service;
    private final StatsService statsService;
    private final ProductApiMapper mapper;

    @GetMapping
    @Operation(
            summary = "List products",
            description = "Returns a paginated list of products ordered alphabetically by name, with identifier as a stable tiebreaker. Optional filters are combined with AND, use inclusive min/max ranges, and search matches product name and description."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned",
                    content = @Content(schema = @Schema(implementation = ProductPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductPageResponse findAll(
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0")
            int page,
            @Parameter(description = "Number of items per page, between 1 and 100", example = "20")
            @RequestParam(defaultValue = "20")
            int size,
            @Parameter(description = "Free text search applied to product name and description after trimming and lowercasing", example = "apple")
            @RequestParam(required = false)
            String search,
            @Parameter(description = "Minimum calories per 100 grams, inclusive", example = "50")
            @RequestParam(required = false)
            java.math.BigDecimal caloriesMin,
            @Parameter(description = "Maximum calories per 100 grams, inclusive", example = "200")
            @RequestParam(required = false)
            java.math.BigDecimal caloriesMax,
            @Parameter(description = "Minimum carbohydrates per 100 grams, inclusive", example = "10")
            @RequestParam(required = false)
            java.math.BigDecimal carbohydratesMin,
            @Parameter(description = "Maximum carbohydrates per 100 grams, inclusive", example = "30")
            @RequestParam(required = false)
            java.math.BigDecimal carbohydratesMax,
            @Parameter(description = "Minimum proteins per 100 grams, inclusive", example = "1")
            @RequestParam(required = false)
            java.math.BigDecimal proteinsMin,
            @Parameter(description = "Maximum proteins per 100 grams, inclusive", example = "40")
            @RequestParam(required = false)
            java.math.BigDecimal proteinsMax,
            @Parameter(description = "Minimum fats per 100 grams, inclusive", example = "0")
            @RequestParam(required = false)
            java.math.BigDecimal fatsMin,
            @Parameter(description = "Maximum fats per 100 grams, inclusive", example = "10")
            @RequestParam(required = false)
            java.math.BigDecimal fatsMax,
            @Parameter(description = "Optional product identifiers for bulk resolution; at most 100", example = "1,2,3")
            @RequestParam(required = false)
            java.util.List<Long> ids
    ) {
        if (ids != null) {
            if (search != null || caloriesMin != null || caloriesMax != null || carbohydratesMin != null || carbohydratesMax != null
                    || proteinsMin != null || proteinsMax != null || fatsMin != null || fatsMax != null) {
                throw new IllegalArgumentException("ids cannot be combined with other product filters");
            }
            var products = service.findByIds(ids);
            int start = Math.min(PaginationRequest.of(page, size).offset(), products.size());
            int end = Math.min(start + size, products.size());
            return new ProductPageResponse(products.subList(start, end).stream().map(mapper::toResponse).toList(),
                    page, size, products.size(), (int) Math.ceil((double) products.size() / size));
        }
        PageResult<com.eliascanalesnieto.foodhelper.domain.Product> result = service.findPage(
                PaginationRequest.of(page, size),
                ProductSearchCriteria.of(search, caloriesMin, caloriesMax, carbohydratesMin, carbohydratesMax, proteinsMin, proteinsMax, fatsMin, fatsMax)
        );
        return new ProductPageResponse(result.items().stream()
                .map(mapper::toResponse)
                .toList(), result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product",
            description = "Returns a single product by identifier, including derived product data when the product comes from a recipe."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product returned",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Product statistics",
            description = "Returns aggregated product and stock statistics calculated on the server from the current database state."
    )
    @ApiResponse(responseCode = "200", description = "Product statistics returned",
            content = @Content(schema = @Schema(implementation = ProductStatsResponse.class)))
    public ProductStatsResponse stats() {
        return statsService.getProductStats();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create product",
            description = "Creates a product with name, description, nutritional values, an optional default price, and zero or more supermarket assignments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Assigned supermarket not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict while creating the product",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return mapper.toResponse(service.create(
                request.name(),
                request.description(),
                request.gramsPerUnit(),
                Boolean.TRUE.equals(request.isStockInUnits()),
                request.calories(),
                request.carbohydrates(),
                request.proteins(),
                request.fats(),
                request.defaultPrice(),
                request.photo() == null ? null : request.photo().toDomain(),
                request.supermarketIds()
        ));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = "Updates an existing product, replaces its nutritional data and supermarket assignments, and can change its optional default price."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product or assigned supermarket not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict while updating the product",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return mapper.toResponse(service.update(
                id,
                request.name(),
                request.description(),
                request.gramsPerUnit(),
                Boolean.TRUE.equals(request.isStockInUnits()),
                request.calories(),
                request.carbohydrates(),
                request.proteins(),
                request.fats(),
                request.defaultPrice(),
                request.photo() == null ? null : request.photo().toDomain(),
                request.supermarketIds()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete product",
            description = "Deletes a product by identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
