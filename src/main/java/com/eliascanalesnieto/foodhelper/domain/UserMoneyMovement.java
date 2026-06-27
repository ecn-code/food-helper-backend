package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserMoneyMovement {
    Long id;
    Long userId;
    BigDecimal amount;
    String description;
    Long currentWeekMenuId;
    Instant createdAt;
}
