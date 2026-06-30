package com.eliascanalesnieto.foodhelper.domain;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import java.util.List;

public interface CurrentWeekMenuRepository {
    CurrentWeekMenuResponse create(CurrentWeekMenuResponse menu);

    CurrentWeekMenuResponse save(CurrentWeekMenuResponse menu);

    CurrentWeekMenuResponse findById(Long id);

    CurrentWeekMenuResponse findByProposedWeekMenuId(Long proposedWeekMenuId);

    List<CurrentWeekMenuResponse> findAll();

    void delete(Long id);
}
