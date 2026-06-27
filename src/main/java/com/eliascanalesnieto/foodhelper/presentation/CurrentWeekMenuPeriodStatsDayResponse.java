package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "MenuPeriodStatsDayResponse", description = "Calories for one day inside a closed menu or month period")
public record CurrentWeekMenuPeriodStatsDayResponse(
        @Schema(description = "Day date", example = "2026-06-21")
        LocalDate date,
        @Schema(description = "Calories for that day", example = "1450.25")
        BigDecimal calories
) {
}
