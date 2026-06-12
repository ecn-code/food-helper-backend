package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ProposedWeekMenuSectionResponse", description = "API representation of one ordered proposed menu section")
public record ProposedWeekMenuSectionResponse(
        @Schema(description = "Section identifier", example = "1")
        Long id,
        @Schema(description = "Section name", example = "Lunch")
        String name,
        @Schema(description = "Explicit display order within the day", example = "10")
        Integer sortOrder,
        @Schema(description = "Ordered products consumed in this section")
        List<ProposedWeekMenuProductResponse> products,
        @Schema(description = "Nutritional totals for the section")
        NutritionalValuesResponse nutritionalValues
) {
}
