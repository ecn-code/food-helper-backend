package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nutritional-rules")
@RequiredArgsConstructor
@Tag(name = "Nutritional rules", description = "Configure daily and weekly nutritional limits used by planning and menus")
public class NutritionalRulesController {
    private final NutritionalRulesService service;

    @GetMapping
    @Operation(summary = "Get nutritional rules", description = "Returns the saved daily and weekly minimum and maximum for each nutrient. Unconfigured limits are null.")
    public NutritionalRulesResponse find() {
        return service.find();
    }

    @PutMapping
    @Operation(summary = "Save nutritional rules", description = "Replaces the daily and weekly nutritional limits. Each nutrient may define a minimum, a maximum, both, or neither in each period.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nutritional rules saved"),
            @ApiResponse(responseCode = "400", description = "Invalid limits", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public NutritionalRulesResponse save(@Valid @RequestBody SaveNutritionalRulesRequest request) {
        return service.save(request);
    }
}
