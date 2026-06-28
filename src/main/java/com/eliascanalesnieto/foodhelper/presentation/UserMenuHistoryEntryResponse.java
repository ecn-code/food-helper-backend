package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "UserMenuHistoryEntryResponse", description = "Immutable closed-menu entry assigned to one person")
public record UserMenuHistoryEntryResponse(
        Long menuId,
        LocalDate startDate,
        LocalDate endDate,
        CurrentWeekMenuPeriodStatsResponse stats
) {
}
