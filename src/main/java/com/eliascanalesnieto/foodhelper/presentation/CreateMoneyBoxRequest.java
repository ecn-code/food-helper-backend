package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateMoneyBoxRequest", description = "Payload for creating a manual money box")
public record CreateMoneyBoxRequest(
        @Schema(description = "Unique manual money box name", example = "Household cash")
        @NotBlank @Size(max = 150) String name
) {
}
