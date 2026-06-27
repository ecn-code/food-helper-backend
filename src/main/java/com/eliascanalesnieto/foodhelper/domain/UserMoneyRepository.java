package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;

public interface UserMoneyRepository {
    UserMoneyMovement addMovement(Long userId, BigDecimal amount, String description, Long currentWeekMenuId);

    UserMoneyBox findMoneyBox(Long userId);
}
