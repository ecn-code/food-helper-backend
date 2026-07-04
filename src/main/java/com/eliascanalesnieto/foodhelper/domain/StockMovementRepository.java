package com.eliascanalesnieto.foodhelper.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StockMovementRepository {
    StockMovement record(StockMovement movement);

    List<StockMovement> findPage(int offset, int limit, LocalDate fromDate, LocalDate toDate, Collection<Long> productIds);

    long count(LocalDate fromDate, LocalDate toDate, Collection<Long> productIds);

    StockMovementTotals summarizeByProduct(Long productId);
}
