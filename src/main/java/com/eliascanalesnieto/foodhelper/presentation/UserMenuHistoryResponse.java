package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "UserMenuHistoryResponse", description = "Monthly or annual immutable menu history for one person")
public record UserMenuHistoryResponse(
        Long personId,
        String personName,
        Integer year,
        Integer month,
        CurrentWeekMenuPeriodStatsResponse totals,
        List<UserMenuHistoryEntryResponse> menus
) {
}
