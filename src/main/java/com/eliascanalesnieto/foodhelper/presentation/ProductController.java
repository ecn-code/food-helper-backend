package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.PageResult;
import com.eliascanalesnieto.foodhelper.application.PaginationRequest;
import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.application.StatsService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
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
            description = "Returns a paginated list of products ordered by identifier ascending."
    )
    @ApiResponse(responseCode = "200", description = "Products returned",
            content = @Content(schema = @Schema(implementation = ProductPageResponse.class)))
    public ProductPageResponse findAll(
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0")
            int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page, between 1 and 100", example = "20")
            @RequestParam(defaultValue = "20")
            int size
    ) {
        PageResult<com.eliascanalesnieto.foodhelper.domain.Product> result = service.findPage(PaginationRequest.of(page, size));
        return new ProductPageResponse(result.items().stream()
                .map(mapper::toResponse)
                .toList(), result.page(), result.size(), result.totalElements(), result.totalPages());
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
            description = "Creates a product with name, description, nutritional values, and an optional default price."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Conflict while creating the product",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return mapper.toResponse(service.create(
                request.name(),
                request.description(),
                request.gramsPerUnit(),
                request.calories(),
                request.carbohydrates(),
                request.proteins(),
                request.fats(),
                request.defaultPrice(),
                request.photo() == null ? null : request.photo().toDomain()
        ));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product",
            description = "Updates an existing product, replaces its nutritional data, and can change its optional default price."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
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
                request.calories(),
                request.carbohydrates(),
                request.proteins(),
                request.fats(),
                request.defaultPrice(),
                request.photo() == null ? null : request.photo().toDomain()
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
