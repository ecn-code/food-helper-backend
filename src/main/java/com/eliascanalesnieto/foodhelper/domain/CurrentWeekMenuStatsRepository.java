package com.eliascanalesnieto.foodhelper.domain;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import java.time.YearMonth;
import java.util.List;

public interface CurrentWeekMenuStatsRepository {
    CurrentWeekMenuStatsResponse save(CurrentWeekMenuStatsResponse stats);

    CurrentWeekMenuStatsResponse findByCurrentWeekMenuId(Long currentWeekMenuId);

    CurrentWeekMenuStatsResponse findByProposedWeekMenuId(Long proposedWeekMenuId);

    List<CurrentWeekMenuResponse> findClosedWeekMenusByMonth(YearMonth month);
}
