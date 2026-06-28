package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "UpdateUserWeightRequest", description = "Replacement values for a stored weight measurement")
public record UpdateUserWeightRequest(
        @NotNull
        @Positive
        @Schema(description = "Weight in kilograms", example = "71.80")
        BigDecimal weight,
        @NotNull
        @Schema(description = "Date and time when the weight was measured", example = "2026-06-29T08:15:00Z")
        Instant recordedAt
) {
}
