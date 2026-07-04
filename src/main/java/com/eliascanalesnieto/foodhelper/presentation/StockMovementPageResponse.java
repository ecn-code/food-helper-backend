package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "StockMovementPageResponse", description = "Paginated list of historical stock movements")
public record StockMovementPageResponse(
        @ArraySchema(schema = @Schema(implementation = StockMovementResponse.class))
        List<StockMovementResponse> items,
        @Schema(description = "Zero-based page number", example = "0")
        int page,
        @Schema(description = "Requested page size", example = "20")
        int size,
        @Schema(description = "Total number of movements", example = "42")
        long totalElements,
        @Schema(description = "Total number of pages", example = "3")
        int totalPages
) {
}
