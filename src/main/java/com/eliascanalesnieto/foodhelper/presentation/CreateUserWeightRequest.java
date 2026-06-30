package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "CreateUserWeightRequest", description = "Weight measurement with its exact recording date and time")
public record CreateUserWeightRequest(
        @NotNull
        @Positive
        @Schema(description = "Weight in kilograms", example = "72.35")
        BigDecimal weight,
        @NotNull
        @Schema(description = "Date and time when the weight was recorded", example = "2026-06-28T08:30:00Z")
        Instant recordedAt,
        @Schema(description = "Optional note attached to the measurement", nullable = true, example = "After breakfast")
        String notes
) {
    public CreateUserWeightRequest(BigDecimal weight, Instant recordedAt) {
        this(weight, recordedAt, null);
    }
}
