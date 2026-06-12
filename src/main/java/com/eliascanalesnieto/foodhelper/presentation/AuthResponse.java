package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "AuthResponse", description = "JWT authentication response")
public record AuthResponse(
        @Schema(description = "Created or authenticated user identifier", example = "1")
        Long userId,
        @Schema(description = "Authenticated username", example = "elias")
        String username,
        @Schema(description = "Signed JWT access token")
        String accessToken,
        @Schema(description = "Authorization scheme", example = "Bearer")
        String tokenType,
        @Schema(description = "UTC expiration instant for the token")
        Instant expiresAt
) {
}
