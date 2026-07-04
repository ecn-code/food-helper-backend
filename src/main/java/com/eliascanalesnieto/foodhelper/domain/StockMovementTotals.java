package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;

public record StockMovementTotals(
        BigDecimal calculatedQuantity,
        BigDecimal totalIn,
        BigDecimal totalOut,
        long movementCount
) {
}
