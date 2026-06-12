package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Payload for obtaining a JWT access token")
public record LoginRequest(
        @Schema(description = "Registered username", example = "elias")
        @NotBlank String username,
        @Schema(description = "User password", example = "correct-horse-battery-staple")
        @NotBlank String password
) {
}
