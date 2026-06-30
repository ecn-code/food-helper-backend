package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserWeightEntry {
    Long id;
    Long userId;
    BigDecimal weight;
    Instant recordedAt;
    String notes;
    Instant createdAt;
    Instant updatedAt;
}
