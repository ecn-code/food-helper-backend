package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "MenuRangeStatsResponse", description = "Aggregated menu statistics for an inclusive date range")
public record CurrentWeekMenuRangeStatsResponse(
        @Schema(description = "Start date of the requested range", example = "2026-06-01")
        LocalDate from,
        @Schema(description = "End date of the requested range", example = "2026-06-30")
        LocalDate to,
        @Schema(description = "Number of planned days that fall inside the range", example = "14")
        long plannedDays,
        @Schema(description = "Total calories planned inside the range", example = "21500.50")
        BigDecimal calories,
        @Schema(description = "Number of distinct products included in the range", example = "18")
        long distinctProducts,
        @Schema(description = "Estimated cost of the menus included in the range", example = "54.30")
        BigDecimal estimatedCost,
        @Schema(description = "Identifiers of the menus contributing to the range")
        List<Long> menuIds
) {
}
