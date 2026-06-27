package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(name = "CreateUserMoneyMovementRequest", description = "Signed money movement to add to a user's money box")
public record CreateUserMoneyMovementRequest(
        @NotNull
        @Schema(description = "Signed amount. Positive values add money and negative values subtract money.", example = "-12.50")
        BigDecimal amount,
        @Schema(description = "Optional movement description", example = "Weekly groceries")
        String description
) {
}
