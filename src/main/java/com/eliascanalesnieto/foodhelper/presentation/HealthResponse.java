package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HealthResponse", description = "Simple API availability response")
public record HealthResponse(
        @Schema(description = "API status", example = "UP")
        String status
) {
}
