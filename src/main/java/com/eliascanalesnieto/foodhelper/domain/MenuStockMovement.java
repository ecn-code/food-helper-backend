package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class MenuStockMovement {
    Long id;
    Long currentWeekMenuId;
    Long userId;
    String userUsername;
    Long productId;
    String productName;
    BigDecimal quantity;
    BigDecimal price;
    BigDecimal totalCost;
    String description;
    LocalDateTime createdAt;
}
