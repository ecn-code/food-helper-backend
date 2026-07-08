package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StockRepository {
    default StockEntry create(Long productId, StockEntry stockEntry) {
        return create(productId, stockEntry, StockMovementType.ENTRY, stockEntry.getEntryDate());
    }

    StockEntry create(Long productId, StockEntry stockEntry, StockMovementType movementType, LocalDate effectiveDate);

    StockEntry findById(Long stockEntryId);

    default StockEntry update(Long stockEntryId, StockEntry stockEntry) {
        return update(stockEntryId, stockEntry, StockMovementType.ADJUSTMENT, LocalDate.now());
    }

    StockEntry update(Long stockEntryId, StockEntry stockEntry, StockMovementType movementType, LocalDate effectiveDate);

    default StockEntry addQuantity(Long stockEntryId, BigDecimal quantity) {
        return addQuantity(stockEntryId, quantity, StockMovementType.ADJUSTMENT, LocalDate.now());
    }

    StockEntry addQuantity(Long stockEntryId, BigDecimal quantity, StockMovementType movementType, LocalDate effectiveDate);

    default void removeQuantity(Long stockEntryId, BigDecimal quantity) {
        removeQuantity(stockEntryId, quantity, StockMovementType.ADJUSTMENT, LocalDate.now());
    }

    void removeQuantity(Long stockEntryId, BigDecimal quantity, StockMovementType movementType, LocalDate effectiveDate);

    default void delete(Long stockEntryId) {
        delete(stockEntryId, StockMovementType.ADJUSTMENT, LocalDate.now());
    }

    void delete(Long stockEntryId, StockMovementType movementType, LocalDate effectiveDate);

    default void restore(CurrentWeekMenuUsedStock usedStock) {
        restore(usedStock, StockMovementType.ADJUSTMENT, LocalDate.now());
    }

    void restore(CurrentWeekMenuUsedStock usedStock, StockMovementType movementType, LocalDate effectiveDate);

    List<StockEntry> findStock(LocalDate expiresBefore, Collection<Long> productIds);
}
