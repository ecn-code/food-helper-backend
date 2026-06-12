package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@Schema(name = "ProposedWeekMenuSectionRequest", description = "Ordered meal section inside one proposed day")
public record ProposedWeekMenuSectionRequest(
        @Schema(description = "Section name", example = "Lunch")
        @NotBlank String name,
        @Schema(description = "Explicit display order within the day", example = "10")
        @NotNull @PositiveOrZero Integer sortOrder,
        @Schema(description = "Ordered products consumed in this section")
        @NotEmpty List<@Valid ProposedWeekMenuProductRequest> products
) {
}
