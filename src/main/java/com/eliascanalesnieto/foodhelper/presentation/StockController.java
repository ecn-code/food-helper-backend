package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.StockService;
import com.eliascanalesnieto.foodhelper.application.PaginationRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Manage live stock entries, inspect inventory ordered by expiration, and review historical stock movements")
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

    @PutMapping("/api/v1/stock/{stockEntryId}")
    @Operation(
            summary = "Update stock entry",
            description = "Updates the editable fields of an existing stock entry: quantity, price, expiration date, and entry date. The linked product stays unchanged."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock entry updated",
                    content = @Content(schema = @Schema(implementation = StockEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Stock entry not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public StockEntryResponse update(
            @PathVariable Long stockEntryId,
            @Valid @RequestBody UpdateStockEntryRequest request
    ) {
        return mapper.toResponse(service.update(
                stockEntryId,
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

    @GetMapping("/api/v1/stock/movements")
    @Operation(
            summary = "List stock movements",
            description = "Returns the historical stock ledger ordered by effective date and recorded timestamp descending, with optional filters by date range and product identifiers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historical stock movements returned",
                    content = @Content(schema = @Schema(implementation = StockMovementPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public StockMovementPageResponse findMovements(
            @Parameter(description = "Inclusive start date for the history filter", example = "2026-06-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @Parameter(description = "Inclusive end date for the history filter", example = "2026-06-30")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @Parameter(description = "Optional product identifiers to filter stock movements", example = "1,2")
            @RequestParam(required = false)
            List<Long> productIds,
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0")
            int page,
            @Parameter(description = "Number of items per page, between 1 and 100", example = "20")
            @RequestParam(defaultValue = "20")
            int size
    ) {
        var result = service.findMovements(PaginationRequest.of(page, size), fromDate, toDate, productIds);
        return new StockMovementPageResponse(
                result.items().stream().map(mapper::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    @GetMapping("/api/v1/products/{productId}/stock/reconciliation")
    @Operation(
            summary = "Reconcile stock for product",
            description = "Compares the historical quantity calculated from the stock ledger with the live stock quantity stored in current stock entries."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock reconciliation returned",
                    content = @Content(schema = @Schema(implementation = StockReconciliationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public StockReconciliationResponse reconcile(@PathVariable Long productId) {
        return service.reconcileProduct(productId);
    }
}
