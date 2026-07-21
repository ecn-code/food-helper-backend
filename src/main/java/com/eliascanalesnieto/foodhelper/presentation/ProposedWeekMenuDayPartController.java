package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuDayPartService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/planning/day-parts")
@RequiredArgsConstructor
@Tag(name = "Planning", description = "Configure reusable day parts used by planning")
public class ProposedWeekMenuDayPartController {
    private final ProposedWeekMenuDayPartService service;

    @GetMapping
    @Operation(
            summary = "List planning day parts",
            description = "Returns all reusable day parts ordered by the configured sort order and identifier."
    )
    @ApiResponse(responseCode = "200", description = "Day parts returned")
    public List<ProposedWeekMenuDayPartResponse> findAll() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create planning day part",
            description = "Creates a reusable day part that can later be referenced by planned days."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Day part created",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuDayPartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuDayPartResponse create(@Valid @RequestBody ProposedWeekMenuDayPartRequest request) {
        return toResponse(service.create(request.name(), request.description(), request.sortOrder()));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update planning day part",
            description = "Updates an existing reusable day part configuration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Day part updated",
                    content = @Content(schema = @Schema(implementation = ProposedWeekMenuDayPartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Day part not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProposedWeekMenuDayPartResponse update(@PathVariable Long id, @Valid @RequestBody ProposedWeekMenuDayPartRequest request) {
        return toResponse(service.update(id, request.name(), request.description(), request.sortOrder()));
    }

    @PutMapping("/order")
    @Operation(summary = "Reorder planning day parts", description = "Validates that the payload contains every current day-part identifier exactly once, then atomically assigns orders 10, 20, 30 and so on.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Day parts reordered"),
            @ApiResponse(responseCode = "400", description = "Identifiers are invalid, duplicated, unknown, or incomplete",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ProposedWeekMenuDayPartResponse> reorder(
            @Valid @RequestBody ReorderProposedWeekMenuDayPartsRequest request
    ) {
        return service.reorder(request.dayPartIds()).stream().map(this::toResponse).toList();
    }

    private ProposedWeekMenuDayPartResponse toResponse(com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPart dayPart) {
        return new ProposedWeekMenuDayPartResponse(
                dayPart.getId(),
                dayPart.getName(),
                dayPart.getDescription(),
                dayPart.getSortOrder()
        );
    }
}
