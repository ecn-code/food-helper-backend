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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Select people and inspect their immutable menu history")
public class UserController {
    private final CurrentWeekMenuService menuService;

    @GetMapping
    @Operation(summary = "List people", description = "Returns the people available when closing a menu.")
    @ApiResponse(responseCode = "200", description = "People returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    public List<UserResponse> findAll() {
        return menuService.findPeople();
    }

    @GetMapping("/{personId}/menu-history/monthly")
    @Operation(summary = "Get monthly menu history", description = "Returns immutable closed-menu history for one person and month.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly history returned", content = @Content(schema = @Schema(implementation = UserMenuHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserMenuHistoryResponse findMonthlyHistory(
            @PathVariable Long personId, @RequestParam int year, @RequestParam int month
    ) {
        return menuService.findMonthlyHistory(personId, year, month);
    }

    @GetMapping("/{personId}/menu-history/annual")
    @Operation(summary = "Get annual menu history", description = "Returns immutable closed-menu history for one person and year.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Annual history returned", content = @Content(schema = @Schema(implementation = UserMenuHistoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public UserMenuHistoryResponse findAnnualHistory(
            @PathVariable Long personId, @RequestParam int year
    ) {
        return menuService.findAnnualHistory(personId, year);
    }
}
