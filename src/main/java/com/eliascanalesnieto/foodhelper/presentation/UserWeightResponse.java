package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "UserWeightResponse", description = "Stored user weight measurement")
public record UserWeightResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long userId,
        @Schema(description = "Weight in kilograms", example = "72.35") BigDecimal weight,
        @Schema(description = "Exact measurement date and time", example = "2026-06-28T08:30:00Z") Instant recordedAt
) {
}
