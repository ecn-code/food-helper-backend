package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/planning")
@RequiredArgsConstructor
@Tag(name = "Planning", description = "Plan a menu for any date range, including recipe productions, before creating it")
public class ProposedWeekMenuController {
    private final ProposedWeekMenuService service;
    private final CurrentWeekMenuService currentWeekMenuService;
    private final ProposedWeekMenuApiMapper mapper;

    @GetMapping
    @Operation(
            summary = "List planning",
            description = "Returns compact planning summaries ordered by start date descending. Full days, products, and calculations are available from the detail endpoint."
    )
    @ApiResponse(responseCode = "200", description = "Planning summaries returned")
    public List<PlanningSummaryResponse> findAll() {
        return service.findAllSummaries().stream()
                .map(summary -> new PlanningSummaryResponse(
                        summary.id(), summary.startDate(), summary.endDate(), summary.plannedDays(),
                        summary.state(), summary.menuId()
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create planning",
            description = "Starts empty menu planning for an inclusive date range of up to 16 calendar days. Planning may contain fewer days than the date range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Planning created",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or overlapping planning",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuResponse create(@Valid @RequestBody CreateProposedWeekMenuRequest request) {
        return mapper.toResponse(service.create(request.startDate(), request.endDate()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get planning",
            description = "Returns planning with ordered days, configured day parts, products, nutritional totals, rule evaluation, and a stock preview."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planning returned",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Planning not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete planning",
            description = "Deletes a planning by identifier. Related days, sections, products, and established menu snapshots are removed automatically.",
            operationId = "deletePlanning"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Planning deleted"),
            @ApiResponse(responseCode = "404", description = "Planning not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/days")
    @Operation(
            summary = "Create or replace planned day",
            description = "Creates or replaces one planned day. Each selected day part can appear only once, products keep their explicit order, manual items can add nutrition without a catalog product, and optional recipe productions generate stock instead of being eaten."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planning updated",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Planning or product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuResponse upsertDay(
            @PathVariable Long id,
            @Valid @RequestBody UpsertProposedWeekMenuDayRequest request
    ) {
        return mapper.toResponse(service.upsertDay(id, mapper.toDomain(request)));
    }

    @PostMapping("/{id}/menu")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create menu from planning",
            description = "Consumes the user-confirmed stock allocation, or automatically uses the earliest expiration date when no allocation is supplied. It creates a menu snapshot, stores missing products as a shopping list, subtracts the applied stock cost from the selected payer user's money box, and carries recipe productions into the menu snapshot."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu created",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Planning or payer user not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse establish(
            @PathVariable Long id,
            @Valid @RequestBody EstablishProposedWeekMenuRequest request
    ) {
        return currentWeekMenuService.establishFromProposed(
                id,
                request.payerUserId(),
                request.stockAllocations()
        );
    }
}
