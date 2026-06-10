package com.eliascanalesnieto.foodhelper.presentation.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "ApiError", description = "Standard API error")
public record ApiError(
        @Schema(description = "Time when the error occurred")
        Instant timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "Short HTTP error name", example = "Bad Request")
        String error,
        @Schema(description = "Functional error detail", example = "name must not be blank")
        String message,
        @Schema(description = "Request path where the error occurred", example = "/api/v1/products")
        String path
) {
}
