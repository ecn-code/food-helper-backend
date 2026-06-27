package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateMenuRequest", description = "Request used to create a menu from planning and assign its cost")
public record EstablishProposedWeekMenuRequest(
        @NotNull
        @Schema(description = "User identifier that assumes the menu cost", example = "1")
        Long payerUserId
) {
}
