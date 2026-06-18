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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/proposed-week-menus")
@RequiredArgsConstructor
@Tag(name = "Proposed week menus", description = "Draft weekly menu planning operations before a menu becomes final")
public class ProposedWeekMenuController {
    private final ProposedWeekMenuService service;
    private final CurrentWeekMenuService currentWeekMenuService;
    private final ProposedWeekMenuApiMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create proposed week menu",
            description = "Starts an empty proposed week menu with an inclusive date range. The range may cover up to 8 calendar days, such as Monday to Monday, and proposed menus can contain only the days planned so far."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proposed week menu created",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuResponse create(@Valid @RequestBody CreateProposedWeekMenuRequest request) {
        return mapper.toResponse(service.create(request.startDate(), request.endDate()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get proposed week menu",
            description = "Returns one proposed week menu with ordered days, configured day parts, products, nutritional totals, and a stock preview summary."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposed week menu returned",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Proposed week menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PutMapping("/{id}/days")
    @Operation(
            summary = "Create or replace proposed day menu",
            description = "Creates or replaces one proposed day menu. Each selected day part can appear only once and products keep their explicit order, which must be unique within each section."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposed week menu updated",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Proposed week menu or product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuResponse upsertDay(
            @PathVariable Long id,
            @Valid @RequestBody UpsertProposedWeekMenuDayRequest request
    ) {
        return mapper.toResponse(service.upsertDay(id, mapper.toDomain(request)));
    }

    @PostMapping("/{id}/publish")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Publish proposed week menu",
            description = "Consumes available stock for the proposed week menu, creates an established week snapshot, and stores the missing products as a shopping list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Established week menu created",
                    content = @Content(schema = @Schema(implementation = CurrentWeekMenuResponse.class))),
            @ApiResponse(responseCode = "404", description = "Proposed week menu not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CurrentWeekMenuResponse publish(@PathVariable Long id) {
        return currentWeekMenuService.publishFromProposed(id);
    }
}
