package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(name = "CreateMenuItemImportsRequest", description = "Purchased item rows imported atomically for an open menu")
public record CreateMenuItemImportsRequest(
        @NotEmpty
        @Valid
        @Schema(description = "Rows to validate and apply in one transaction")
        List<MenuItemImportRequest> items
) {
}
