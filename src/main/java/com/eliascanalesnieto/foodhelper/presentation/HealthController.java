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
@Tag(name = "Health", description = "Basic API status")
public class HealthController {

    @GetMapping
    @Operation(summary = "API status", description = "Returns a simple availability indicator.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API available",
                    content = @Content(schema = @Schema(implementation = HealthResponse.class)))
    })
    public HealthResponse health() {
        return new HealthResponse("UP");
    }
}
