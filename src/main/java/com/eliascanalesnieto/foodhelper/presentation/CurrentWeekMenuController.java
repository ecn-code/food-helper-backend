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
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "Menus", description = "Inspect established week snapshots, consumed stock, week stock, recipe-to-stock transfers, missing products, and closure stats")
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
            description = "Returns one menu snapshot with ordered days, nutritional totals, rule evaluation, stock summary, consumed stock, missing products, and assigned people when available."
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
            description = "Returns every consumed quantity to its original stock entry, removes the linked money movement, reverses transferred recipe stock when possible, and deletes the open menu so its planning can be established again. Closed menus cannot be undone."
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

    @PostMapping("/{id}/recipe-productions/{recipeProductionId}/transfer")
    @Operation(
            summary = "Transfer recipe production to stock",
            description = "Creates a stock entry from one recipe production inside the menu, records the transfer trace in the menu snapshot, and refuses to transfer the same production more than once."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipe production transferred",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Menu or recipe production not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "400", description = "Menu cannot be modified or recipe production was already transferred",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse transferRecipeProduction(
            @PathVariable Long id,
            @PathVariable Long recipeProductionId
    ) {
        return service.transferRecipeProduction(id, recipeProductionId);
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

    @GetMapping("/{id}/stock-movements")
    @Operation(
            summary = "List stock repercussion movements for menu",
            description = "Returns the historical stock repercussion movements recorded while the menu is open."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock repercussion movements returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuStockMovementResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<MenuStockMovementResponse> findStockMovements(@PathVariable Long id) {
        return service.findStockMovements(id);
    }

    @GetMapping("/{id}/shopping-list")
    @Operation(
            summary = "List shopping list for menu",
            description = "Returns the products and missing units that were not covered by stock when the week was established. When supermarketId is provided, products assigned to that supermarket and products without any supermarket assignment are returned. This read-only operation never recalculates the week or changes stock."
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

    @GetMapping("/{id}/week-stock")
    @Operation(
            summary = "List week stock",
            description = "Returns the temporary stock tracked for the established week, separate from the global product stock."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Week stock returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CurrentWeekMenuStockItemResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CurrentWeekMenuStockItemResponse> findWeekStock(@PathVariable Long id) {
        return service.findById(id).weekStock();
    }

    @PutMapping("/{id}/payer")
    @Operation(
            summary = "Update menu responsible",
            description = "Updates the default responsible user used by the front when recording stock repercussion movements."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu responsible updated",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Menu cannot be modified or user is invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Menu or user not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse updateResponsible(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCurrentWeekMenuPayerRequest request
    ) {
        return service.updateResponsible(id, request.userId());
    }

    @PostMapping("/{id}/stock-movements")
    @Operation(
            summary = "Record stock repercussion movement",
            description = "Records a positive stock movement for an open menu, stores the historical ledger entry, and charges the selected user money box using the menu responsible as default when userId is omitted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock repercussion recorded",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Menu cannot be modified or movement is invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Menu, product, or user not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse addStockMovement(
            @PathVariable Long id,
            @Valid @RequestBody CreateMenuStockMovementRequest request
    ) {
        return service.addStockMovement(id, request);
    }

    @PutMapping("/{id}/week-stock")
    @Operation(
            summary = "Replace week stock",
            description = "Replaces the temporary stock tracked for the established week and updates the shopping list accordingly."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Week stock updated",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Week stock payload is invalid or the menu cannot be modified",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Menu or product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse updateWeekStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCurrentWeekMenuStockRequest request
    ) {
        return service.updateWeekStock(id, request);
    }

    @PostMapping("/{id}/close")
    @Operation(
            summary = "Close menu",
            description = "Closes a menu after its end date and saves an immutable history snapshot for every selected person. Repeating a successful close returns the originally saved statistics without duplicating snapshots."
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

    @GetMapping("/stats")
    @Operation(
            summary = "Get menu stats for a date range",
            description = "Returns the aggregated calories, estimated cost, distinct products, and included menu identifiers for the menus whose planned days fall inside the requested inclusive date range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Range stats returned",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuRangeStatsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Range is invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuRangeStatsResponse findStatsByRange(
            @io.swagger.v3.oas.annotations.Parameter(description = "Inclusive start date for the range", example = "2026-06-01")
            @RequestParam LocalDate from,
            @io.swagger.v3.oas.annotations.Parameter(description = "Inclusive end date for the range", example = "2026-06-30")
            @RequestParam LocalDate to
    ) {
        return service.findStatsByRange(from, to);
    }
}
