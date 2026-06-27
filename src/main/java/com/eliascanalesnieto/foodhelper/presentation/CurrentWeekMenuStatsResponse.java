package com.eliascanalesnieto.foodhelper.presentation;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MenuStatsResponse", description = "Closed menu statistics split into menu-period and month aggregates")
public record CurrentWeekMenuStatsResponse(
        @Schema(description = "Identifier of the menu")
        @JsonAlias("currentWeekMenuId") Long menuId,
        @Schema(description = "Statistics for the complete menu period")
        @JsonAlias("week") CurrentWeekMenuPeriodStatsResponse period,
        @Schema(description = "Statistics for the month in which the menu ends")
        CurrentWeekMenuPeriodStatsResponse month
) {
}
