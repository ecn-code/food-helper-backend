package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(name = "UserMoneyBoxResponse", description = "User money box with current balance and signed movements")
public record UserMoneyBoxResponse(
        @Schema(description = "User identifier", example = "1")
        Long userId,
        @Schema(description = "Username", example = "elias")
        String username,
        @Schema(description = "Current balance calculated from all signed movements", example = "87.50")
        BigDecimal balance,
        @Schema(description = "Movements ordered from newest to oldest")
        List<UserMoneyMovementResponse> movements
) {
}
