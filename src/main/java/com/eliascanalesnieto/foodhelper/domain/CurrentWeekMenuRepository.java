package com.eliascanalesnieto.foodhelper.domain;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;

public interface CurrentWeekMenuRepository {
    CurrentWeekMenuResponse create(CurrentWeekMenuResponse menu);

    CurrentWeekMenuResponse findById(Long id);

    CurrentWeekMenuResponse findByProposedWeekMenuId(Long proposedWeekMenuId);
}
