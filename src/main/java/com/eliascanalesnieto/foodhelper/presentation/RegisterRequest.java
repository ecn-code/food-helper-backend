package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Payload for creating an API user")
public record RegisterRequest(
        @Schema(description = "Unique username used to log in", example = "elias")
        @NotBlank @Size(min = 3, max = 80) String username,
        @Schema(description = "Plain password. It is hashed before storage.", example = "correct-horse-battery-staple")
        @NotBlank @Size(min = 8, max = 128) String password,
        @Schema(description = "Shared registration code required to create a user", example = "foodhelper-invite")
        @NotBlank @Size(max = 128) String registrationCode
) {
}
