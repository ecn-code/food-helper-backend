package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(name = "UserMenuHistoryResponse", description = "Immutable menu history for one person in an inclusive date-time range")
public record UserMenuHistoryResponse(
        Long personId,
        String personName,
        Instant from,
        Instant to,
        CurrentWeekMenuPeriodStatsResponse totals,
        List<UserMenuHistoryEntryResponse> menus
) {
}
