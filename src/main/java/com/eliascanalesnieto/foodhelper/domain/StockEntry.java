package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StockEntry {
    Long id;
    Long productId;
    String productName;
    BigDecimal quantity;
    LocalDate expirationDate;
    LocalDate entryDate;
}
