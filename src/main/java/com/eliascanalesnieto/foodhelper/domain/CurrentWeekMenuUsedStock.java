package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CurrentWeekMenuUsedStock {
    Long stockEntryId;
    Long productId;
    String productName;
    BigDecimal usedUnits;
    BigDecimal price;
    BigDecimal totalCost;
    LocalDate expirationDate;
    LocalDate entryDate;
}
