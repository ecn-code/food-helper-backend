package com.eliascanalesnieto.foodhelper.presentation.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "ApiError", description = "Error estandar de la API")
public record ApiError(
        @Schema(description = "Momento en el que se produjo el error")
        Instant timestamp,
        @Schema(description = "Codigo HTTP", example = "400")
        int status,
        @Schema(description = "Nombre corto del error HTTP", example = "Bad Request")
        String error,
        @Schema(description = "Detalle funcional del error", example = "name must not be blank")
        String message,
        @Schema(description = "Ruta donde ocurrio el error", example = "/api/v1/products")
        String path
) {
}
