package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(name = "ChallengeDefinitionRequest", description = "Payload for creating or updating a challenge definition")
public record ChallengeDefinitionRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{1,80}") @Schema(example = "NO_SPEND_DAY") String code,
        @NotBlank @Size(max = 150) @Schema(example = "No-spend day") String name,
        @NotBlank @Schema(example = "Do not spend money for one full day.") String description,
        @NotNull @DecimalMin("0.00") @Schema(example = "10.00") BigDecimal rewardAmount,
        @Min(0) @Schema(example = "30") int periodDays
) {}
