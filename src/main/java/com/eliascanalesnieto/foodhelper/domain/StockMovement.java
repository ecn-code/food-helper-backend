package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class StockMovement {
    Long id;
    Long productId;
    Long stockEntryId;
    String productName;
    StockMovementType movementType;
    BigDecimal signedQuantity;
    LocalDate effectiveDate;
    LocalDateTime recordedAt;
    BigDecimal price;
    LocalDate expirationDate;
    LocalDate entryDate;
}
