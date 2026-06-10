package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Estado basico de la API")
public class HealthController {

    @GetMapping
    @Operation(summary = "Estado de la API", description = "Devuelve un indicador simple de disponibilidad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API disponible",
                    content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    })
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}
