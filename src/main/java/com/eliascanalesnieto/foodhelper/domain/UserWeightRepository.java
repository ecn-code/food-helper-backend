package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface UserWeightRepository {
    UserWeightEntry create(Long userId, BigDecimal weight, Instant recordedAt, String notes);

    UserWeightEntry update(Long userId, Long weightId, BigDecimal weight, Instant recordedAt, String notes);

    void delete(Long userId, Long weightId);

    List<UserWeightEntry> findByPeriod(Long userId, Instant from, Instant to);
}
