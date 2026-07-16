package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "ChallengeResponse", description = "Challenge catalog entry with reward and cooldown availability")
public record ChallengeResponse(
        @Schema(description = "Challenge definition identifier", example = "1") Long id,
        @Schema(description = "Stable challenge code", example = "QUEUES") String code,
        @Schema(description = "Challenge display name", example = "Queue challenge") String name,
        @Schema(description = "Instructions for completing the challenge") String description,
        @Schema(description = "Money added to the user's money box immediately when the challenge is selected", example = "10.00") BigDecimal rewardAmount,
        @Schema(description = "Cooldown in days before the challenge can be selected again", example = "30") int periodDays,
        @Schema(description = "Whether the challenge can be selected now", example = "true") boolean available,
        @Schema(description = "Instant when the challenge was last selected, or null if never selected", nullable = true) Instant lastUsedAt,
        @Schema(description = "Instant when the challenge becomes selectable again, or null when available", nullable = true) Instant nextAvailableAt
) {
}
