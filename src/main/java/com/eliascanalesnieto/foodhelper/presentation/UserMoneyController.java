package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.UserMoneyService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/money-box")
@RequiredArgsConstructor
@Tag(name = "User money boxes", description = "Inspect user money boxes and register signed money movements")
public class UserMoneyController {
    private final UserMoneyService service;

    @GetMapping
    @Operation(
            summary = "Get user money box",
            description = "Returns the user's current money box balance and signed movements ordered from newest to oldest."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money box returned",
                    content = @Content(schema = @Schema(implementation = UserMoneyBoxResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserMoneyBoxResponse findMoneyBox(@PathVariable Long userId) {
        return service.findMoneyBox(userId);
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add user money movement",
            description = "Adds a signed money movement to the user's money box. Positive amounts increase the balance and negative amounts decrease it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Money movement created",
                    content = @Content(schema = @Schema(implementation = UserMoneyMovementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserMoneyMovementResponse addMovement(
            @PathVariable Long userId,
            @Valid @RequestBody CreateUserMoneyMovementRequest request
    ) {
        return service.addMovement(userId, request.amount(), request.description());
    }
}
