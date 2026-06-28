package com.eliascanalesnieto.foodhelper.domain;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import java.time.LocalDate;
import java.util.List;

public interface UserMenuHistoryRepository {
    void save(Long menuId, AppUser person, CurrentWeekMenuResponse menuSnapshot);

    List<CurrentWeekMenuResponse> findMenus(Long personId, LocalDate from, LocalDate to);
}
