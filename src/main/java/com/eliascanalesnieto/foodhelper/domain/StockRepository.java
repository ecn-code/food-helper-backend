package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StockRepository {
    StockEntry create(Long productId, StockEntry stockEntry);

    StockEntry update(Long stockEntryId, StockEntry stockEntry);

    StockEntry addQuantity(Long stockEntryId, BigDecimal quantity);

    void removeQuantity(Long stockEntryId, BigDecimal quantity);

    List<StockEntry> findStock(LocalDate expiresBefore, Collection<Long> productIds);
}
