package com.eliascanalesnieto.foodhelper.presentation;

import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "PositiveStockProductResponse", description = "Per-product quantities calculated before closing a menu")
public record PositiveStockProductResponse(
        Long productId,
        String productName,
        QuantityType quantityType,
        BigDecimal currentGlobalQuantity,
        BigDecimal historicalUsedQuantity,
        BigDecimal weekStockQuantity,
        BigDecimal pendingRecipeQuantity,
        BigDecimal transferableQuantity,
        BigDecimal finalGlobalQuantity,
        BigDecimal estimatedTransferValue
) {
}
