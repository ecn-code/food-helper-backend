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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "Menus", description = "Inspect menu snapshots, consumed stock, missing products, and closure stats")
public class CurrentWeekMenuController {
    private final CurrentWeekMenuService service;

    @GetMapping
    @Operation(
            summary = "List menus",
            description = "Returns all menus that have been created from planning, ordered by identifier."
    )
    @ApiResponse(responseCode = "200", description = "Menus returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuResponse.class))))
    public List<CurrentWeekMenuResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get menu",
            description = "Returns one menu snapshot with ordered days, nutritional totals, rule evaluation, stock summary, consumed stock, and missing products."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu returned",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "undoMenuCreation",
            summary = "Undo menu creation",
            description = "Returns every consumed quantity to its original stock entry, removes the linked money movement, and deletes the open menu so its planning can be established again. Closed menus cannot be undone."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu creation undone"),
            @ApiResponse(responseCode = "400", description = "Closed menu cannot be undone",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void undo(@PathVariable Long id) {
        service.undo(id);
    }

    @GetMapping("/{id}/used-stock")
    @Operation(
            summary = "List used stock for menu",
            description = "Returns the stock entries consumed when the menu was created, ordered by the original stock allocation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consumed stock entries returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuUsedStockResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CurrentWeekMenuUsedStockResponse> findUsedStock(@PathVariable Long id) {
        return service.findById(id).usedStock();
    }

    @GetMapping("/{id}/shopping-list")
    @Operation(
            summary = "List shopping list for menu",
            description = "Returns the products and missing units that were not covered by stock when the menu was created. When supermarketId is provided, only products available at that supermarket are returned. This read-only operation never recalculates the menu or changes stock."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuShoppingListItemResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Menu or supermarket not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CurrentWeekMenuShoppingListItemResponse> findShoppingList(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.Parameter(description = "Optional supermarket identifier used to filter available products", example = "1")
            @RequestParam(required = false) Long supermarketId
    ) {
        return service.findShoppingList(id, supermarketId);
    }

    @PostMapping("/{id}/close")
    @Operation(
            summary = "Close menu",
            description = "Closes a menu after its end date and saves an immutable history snapshot for every selected person."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu closed",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuStatsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "400", description = "Menu cannot be closed yet",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuStatsResponse close(
            @PathVariable Long id,
            @Valid @RequestBody CloseCurrentWeekMenuRequest request
    ) {
        return service.close(id, request.personIds());
    }

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Get menu stats",
            description = "Returns the saved period and month statistics captured when the menu was closed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu stats returned",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuStatsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Menu stats not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuStatsResponse findStats(@PathVariable Long id) {
        return service.findStatsById(id);
    }
}
