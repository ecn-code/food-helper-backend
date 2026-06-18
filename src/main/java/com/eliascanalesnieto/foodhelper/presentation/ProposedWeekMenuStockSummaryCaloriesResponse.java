package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ProposedWeekMenuStockSummaryCaloriesResponse", description = "Calorie distribution for the planned days in a proposed week menu")
public record ProposedWeekMenuStockSummaryCaloriesResponse(
        @Schema(description = "Average calories per planned day", example = "1234.56")
        BigDecimal averagePerPlannedDay,
        @Schema(description = "Planned day with the highest calories", nullable = true)
        ProposedWeekMenuStockSummaryDayCaloriesResponse maxDay,
        @Schema(description = "Planned day with the lowest calories", nullable = true)
        ProposedWeekMenuStockSummaryDayCaloriesResponse minDay
) {
}
