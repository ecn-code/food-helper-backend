package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "UserWeightResponse", description = "Stored user weight measurement")
public record UserWeightResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "1") Long userId,
        @Schema(description = "Weight in kilograms", example = "72.35") BigDecimal weight,
        @Schema(description = "Exact measurement date and time", example = "2026-06-28T08:30:00Z") Instant recordedAt,
        @Schema(description = "Optional note attached to the measurement", nullable = true, example = "After breakfast")
        String notes,
        @Schema(description = "UTC instant when the measurement was created", example = "2026-06-28T08:30:00Z")
        Instant createdAt,
        @Schema(description = "UTC instant when the measurement was last updated", example = "2026-06-29T08:15:00Z")
        Instant updatedAt
) {
}
