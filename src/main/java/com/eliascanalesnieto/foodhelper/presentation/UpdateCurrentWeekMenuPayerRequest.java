package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateCurrentWeekMenuPayerRequest", description = "Request to change the default responsible user for an open menu")
public record UpdateCurrentWeekMenuPayerRequest(
        @NotNull
        @Schema(description = "User identifier used as the menu responsible", example = "2")
        Long userId
) {
}
