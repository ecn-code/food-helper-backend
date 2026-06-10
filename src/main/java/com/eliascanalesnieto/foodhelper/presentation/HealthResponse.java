package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HealthResponse", description = "Respuesta simple de disponibilidad de la API")
public record HealthResponse(
        @Schema(description = "Estado de la API", example = "UP")
        String status
) {
}
