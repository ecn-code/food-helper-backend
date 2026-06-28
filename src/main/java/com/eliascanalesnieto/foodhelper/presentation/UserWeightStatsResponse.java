package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "UserWeightStatsResponse", description = "Highest and lowest user weight measurements in an inclusive period")
public record UserWeightStatsResponse(
        @Schema(example = "1") Long userId,
        @Schema(example = "2026-06-01T00:00:00Z") Instant from,
        @Schema(example = "2026-06-30T23:59:59Z") Instant to,
        @Schema(description = "Highest measurement, or null when the period has no measurements", nullable = true)
        UserWeightResponse highest,
        @Schema(description = "Lowest measurement, or null when the period has no measurements", nullable = true)
        UserWeightResponse lowest
) {
}
