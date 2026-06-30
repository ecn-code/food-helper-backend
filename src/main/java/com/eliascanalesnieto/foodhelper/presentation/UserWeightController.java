package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.application.UserWeightService;
import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/weights")
@RequiredArgsConstructor
@Tag(name = "User weights", description = "Record user weights and inspect measurements and statistics by period")
public class UserWeightController {
    private final UserWeightService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record user weight", description = "Stores a weight in kilograms with its exact measurement date and time.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Weight recorded",
                    content = @Content(schema = @Schema(implementation = UserWeightResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserWeightResponse create(@PathVariable Long userId, @Valid @RequestBody CreateUserWeightRequest request) {
        return service.create(userId, request.weight(), request.recordedAt(), request.notes());
    }

    @PutMapping("/{weightId}")
    @Operation(summary = "Update user weight", description = "Replaces the weight and exact measurement date and time. The measurement must belong to the user in the path.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weight measurement updated",
                    content = @Content(schema = @Schema(implementation = UserWeightResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Weight measurement not found for user",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserWeightResponse update(
            @PathVariable Long userId,
            @PathVariable Long weightId,
            @Valid @RequestBody UpdateUserWeightRequest request
    ) {
        return service.update(userId, weightId, request.weight(), request.recordedAt(), request.notes());
    }

    @DeleteMapping("/{weightId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user weight", description = "Deletes a weight measurement only when it belongs to the user in the path.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Weight measurement deleted"),
            @ApiResponse(responseCode = "404", description = "Weight measurement not found for user",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@PathVariable Long userId, @PathVariable Long weightId) {
        service.delete(userId, weightId);
    }

    @GetMapping
    @Operation(summary = "List user weights by period", description = "Returns measurements in an inclusive date-time period, ordered from oldest to newest.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weight measurements returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserWeightResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid period",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<UserWeightResponse> findByPeriod(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return service.findByPeriod(userId, from, to);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user weight statistics", description = "Returns the highest and lowest measurements, including their exact dates and times, in an inclusive period. For tied weights, the oldest measurement is returned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weight statistics returned",
                    content = @Content(schema = @Schema(implementation = UserWeightStatsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid period",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserWeightStatsResponse findStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return service.findStats(userId, from, to);
    }
}
