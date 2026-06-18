package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/established-week-menus")
@RequiredArgsConstructor
@Tag(name = "Established week menus", description = "Inspect established week snapshots, the stock consumed, and the shopping list for missing products")
public class CurrentWeekMenuController {
    private final CurrentWeekMenuService service;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get established week menu",
            description = "Returns one established week menu snapshot with ordered days, nutritional totals, the stock summary captured at publication time, the consumed stock entries, and the shopping list for missing products."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Established week menu returned",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Established week menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/used-stock")
    @Operation(
            summary = "List used stock for established week menu",
            description = "Returns the stock entries consumed when the established week menu was published, ordered by the original stock allocation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consumed stock entries returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuUsedStockResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Established week menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CurrentWeekMenuUsedStockResponse> findUsedStock(@PathVariable Long id) {
        return service.findById(id).usedStock();
    }

    @GetMapping("/{id}/shopping-list")
    @Operation(
            summary = "List shopping list for established week menu",
            description = "Returns the products and missing units that were not covered by stock when the established week menu was published."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuShoppingListItemResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Established week menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CurrentWeekMenuShoppingListItemResponse> findShoppingList(@PathVariable Long id) {
        return service.findById(id).shoppingList();
    }
}
