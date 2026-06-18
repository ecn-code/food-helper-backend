package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "ProposedWeekMenuStockSummaryDayCaloriesResponse", description = "Calories for one planned day in the proposed week menu")
public record ProposedWeekMenuStockSummaryDayCaloriesResponse(
        @Schema(description = "Day date", example = "2026-06-15")
        LocalDate date,
        @Schema(description = "Calories for the day", example = "1450.25")
        BigDecimal calories
) {
}
