package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "PlanningSectionResponse", description = "API representation of one planning section bound to a configured day part")
public record ProposedWeekMenuSectionResponse(
        @Schema(description = "Section identifier", example = "1")
        Long id,
        @Schema(description = "Reusable day part identifier", example = "1")
        Long dayPartId,
        @Schema(description = "Section name", example = "Lunch")
        String name,
        @Schema(description = "Section description", example = "Main meal of the day")
        String description,
        @Schema(description = "Explicit display order within the day", example = "10")
        Integer sortOrder,
        @Schema(description = "Ordered products consumed in this section")
        List<ProposedWeekMenuProductResponse> products,
        @Schema(description = "Nutritional totals for the section")
        NutritionalValuesResponse nutritionalValues
) {
}
