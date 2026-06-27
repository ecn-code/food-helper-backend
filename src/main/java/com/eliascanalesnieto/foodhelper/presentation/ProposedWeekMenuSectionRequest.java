package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(name = "PlanningSectionRequest", description = "Reusable day part selected inside one planned day. Products inside the section must use unique sortOrder values.")
public record ProposedWeekMenuSectionRequest(
        @Schema(description = "Reusable day part identifier", example = "1")
        @NotNull Long dayPartId,
        @Schema(description = "Ordered products consumed in this section. Each product sortOrder must be unique within the section.")
        @NotEmpty List<@Valid ProposedWeekMenuProductRequest> products
) {
}
