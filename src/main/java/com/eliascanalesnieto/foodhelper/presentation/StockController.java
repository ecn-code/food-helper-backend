package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.StockService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Manage stock entries and inspect inventory ordered by expiration")
public class StockController {
    private final StockService service;
    private final ProductApiMapper mapper;

    @PostMapping("/api/v1/products/{productId}/stock")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create stock entry",
            description = "Creates a stock entry for a product with a positive quantity, a required price, an optional expiration date, and a required entry date."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stock entry created",
                    content = @Content(schema = @Schema(implementation = StockEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public StockEntryResponse create(
            @PathVariable Long productId,
            @Valid @RequestBody CreateStockEntryRequest request
    ) {
        return mapper.toResponse(service.create(
                productId,
                request.quantity(),
                request.price(),
                request.expirationDate(),
                request.entryDate()
        ));
    }

    @PostMapping("/api/v1/stock/{stockEntryId}/add")
    @Operation(
            summary = "Add quantity to stock entry",
            description = "Adds a positive quantity to an existing stock entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock entry updated",
                    content = @Content(schema = @Schema(implementation = StockEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Stock entry not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public StockEntryResponse addQuantity(
            @PathVariable Long stockEntryId,
            @Valid @RequestBody AdjustStockQuantityRequest request
    ) {
        return mapper.toResponse(service.addQuantity(stockEntryId, request.quantity()));
    }

    @PostMapping("/api/v1/stock/{stockEntryId}/remove")
    @Operation(
            summary = "Remove quantity from stock entry",
            description = "Removes a positive quantity from a stock entry. If the remaining quantity becomes zero, the stock entry is deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock quantity removed"),
            @ApiResponse(responseCode = "400", description = "Invalid request or quantity exceeds current stock",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Stock entry not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void removeQuantity(
            @PathVariable Long stockEntryId,
            @Valid @RequestBody AdjustStockQuantityRequest request
    ) {
        service.removeQuantity(stockEntryId, request.quantity());
    }

    @GetMapping("/api/v1/stock")
    @Operation(
            summary = "List stock entries",
            description = "Returns stock entries ordered by expiration date ascending, with optional filters by expiration date and product identifiers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock entries ordered by expiration",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockEntryResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<StockEntryResponse> findStock(
            @Parameter(description = "Only include stock entries with expiration date strictly before this date", example = "2026-06-20")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate expiresBefore,
            @Parameter(description = "Optional product identifiers to filter stock entries", example = "1,2")
            @RequestParam(required = false)
            List<Long> productIds
    ) {
        return service.findStock(expiresBefore, productIds).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/api/v1/products/{productId}/stock")
    @Operation(
            summary = "List stock entries for product",
            description = "Returns stock entries for a single product ordered by expiration date ascending, optionally filtering by expiration date."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product stock entries ordered by expiration",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockEntryResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<StockEntryResponse> findStockByProduct(
            @PathVariable Long productId,
            @Parameter(description = "Only include stock entries with expiration date strictly before this date", example = "2026-06-20")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate expiresBefore
    ) {
        return service.findStockByProduct(productId, expiresBefore).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
