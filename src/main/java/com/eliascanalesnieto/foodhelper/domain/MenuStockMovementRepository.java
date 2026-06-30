package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;

public interface MenuStockMovementRepository {
    MenuStockMovement save(MenuStockMovement movement);

    List<MenuStockMovement> findByCurrentWeekMenuId(Long currentWeekMenuId);

    void deleteByCurrentWeekMenuId(Long currentWeekMenuId);
}
