package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(name = "CloseMenuRequest", description = "People for whom the closed menu must be recorded")
public record CloseCurrentWeekMenuRequest(
        @NotEmpty
        @Schema(description = "Identifiers of the people who followed the menu", example = "[1, 2]")
        List<Long> personIds
) {
}
