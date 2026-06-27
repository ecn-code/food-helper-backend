package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserMoneyBox {
    Long userId;
    String username;
    BigDecimal balance;
    List<UserMoneyMovement> movements;
}
