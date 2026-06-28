package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponse", description = "Person available for menu history assignment")
public record UserResponse(
        @Schema(description = "User identifier", example = "1") Long id,
        @Schema(description = "Username", example = "elias") String username
) {
}
