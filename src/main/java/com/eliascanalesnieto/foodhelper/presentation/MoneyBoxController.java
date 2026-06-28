package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.UserMoneyService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/money-boxes")
@RequiredArgsConstructor
@Tag(name = "Money boxes", description = "Manage user-owned and manually created money boxes")
public class MoneyBoxController {
    private final UserMoneyService service;

    @GetMapping
    @Operation(
            summary = "List all money boxes",
            description = "Returns user-owned and manually created money boxes in one list, including balances and movements."
    )
    @ApiResponse(responseCode = "200", description = "Money boxes returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MoneyBoxResponse.class))))
    public List<MoneyBoxResponse> findAll() {
        return service.findAllMoneyBoxes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create manual money box", description = "Creates a money box that is not owned by a user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Manual money box created",
                    content = @Content(schema = @Schema(implementation = MoneyBoxResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Manual money box name already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public MoneyBoxResponse create(@Valid @RequestBody CreateMoneyBoxRequest request) {
        return service.createManualMoneyBox(request.name());
    }

    @GetMapping("/{moneyBoxId}")
    @Operation(summary = "Get money box", description = "Returns one user-owned or manual money box by identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money box returned",
                    content = @Content(schema = @Schema(implementation = MoneyBoxResponse.class))),
            @ApiResponse(responseCode = "404", description = "Money box not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public MoneyBoxResponse findById(@PathVariable Long moneyBoxId) {
        return service.findMoneyBoxById(moneyBoxId);
    }

    @DeleteMapping("/{moneyBoxId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteManualMoneyBox",
            summary = "Delete manual money box",
            description = "Deletes a manual money box and all its movements in one transaction. User-owned money boxes cannot be deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Manual money box deleted"),
            @ApiResponse(responseCode = "404", description = "Money box not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "User-owned money box cannot be deleted",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable Long moneyBoxId) {
        service.deleteMoneyBox(moneyBoxId);
    }

    @PostMapping("/{moneyBoxId}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add money box movement",
            description = "Adds a signed movement to a user-owned or manual money box."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Money movement created",
                    content = @Content(schema = @Schema(implementation = MoneyBoxMovementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Money box not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public MoneyBoxMovementResponse addMovement(
            @PathVariable Long moneyBoxId,
            @Valid @RequestBody CreateUserMoneyMovementRequest request
    ) {
        return service.addMoneyBoxMovement(moneyBoxId, request.amount(), request.description());
    }

    @DeleteMapping("/{moneyBoxId}/movements/{movementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteMoneyBoxMovement",
            summary = "Delete money box movement",
            description = "Deletes a movement only when it belongs to the selected money box and is not linked to a menu."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Money movement deleted"),
            @ApiResponse(responseCode = "404", description = "Money box movement not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Money movement is linked to a menu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void deleteMovement(@PathVariable Long moneyBoxId, @PathVariable Long movementId) {
        service.deleteMoneyBoxMovement(moneyBoxId, movementId);
    }
}
