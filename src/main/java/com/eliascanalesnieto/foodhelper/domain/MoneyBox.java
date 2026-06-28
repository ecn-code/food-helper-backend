package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MoneyBox {
    Long id;
    MoneyBoxType type;
    String name;
    Long userId;
    String username;
    BigDecimal balance;
    List<UserMoneyMovement> movements;
}
