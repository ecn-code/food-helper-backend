package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;

public interface UserMoneyRepository {
    UserMoneyMovement addMovement(Long userId, BigDecimal amount, String description, Long currentWeekMenuId);

    default UserMoneyMovement addMovement(Long userId, BigDecimal amount, String description) {
        return addMovement(userId, amount, description, null);
    }

    UserMoneyBox findMoneyBox(Long userId);

    MoneyBox createManualMoneyBox(String name);

    List<MoneyBox> findAllMoneyBoxes();

    MoneyBox findMoneyBoxById(Long moneyBoxId);

    UserMoneyMovement addMoneyBoxMovement(Long moneyBoxId, BigDecimal amount, String description);

    void deleteMoneyBox(Long moneyBoxId);

    void deleteMoneyBoxMovement(Long moneyBoxId, Long movementId);

    void deleteMovementsByCurrentWeekMenuId(Long currentWeekMenuId);
}
