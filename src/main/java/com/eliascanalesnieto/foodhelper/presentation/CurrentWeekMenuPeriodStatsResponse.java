package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "MenuPeriodStatsResponse", description = "Aggregated nutrition and spending statistics for one period")
public record CurrentWeekMenuPeriodStatsResponse(
        @Schema(description = "Day with the highest calories", nullable = true)
        CurrentWeekMenuPeriodStatsDayResponse maxDay,
        @Schema(description = "Day with the lowest calories", nullable = true)
        CurrentWeekMenuPeriodStatsDayResponse minDay,
        @Schema(description = "Average calories per day in the period", example = "1450.25")
        BigDecimal averageCalories,
        @Schema(description = "Average carbohydrates per day in the period", example = "120.50")
        BigDecimal averageCarbohydrates,
        @Schema(description = "Average proteins per day in the period", example = "80.10")
        BigDecimal averageProteins,
        @Schema(description = "Average fats per day in the period", example = "45.75")
        BigDecimal averageFats,
        @Schema(description = "Money spent in the period", example = "32.40")
        BigDecimal moneySpent
) {
}
