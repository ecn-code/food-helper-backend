package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockMovement;
import com.eliascanalesnieto.foodhelper.domain.StockMovementRepository;
import com.eliascanalesnieto.foodhelper.domain.StockMovementTotals;
import com.eliascanalesnieto.foodhelper.domain.StockMovementType;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.presentation.StockReconciliationResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {
    private static final int PRICE_SCALE = 4;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final Clock clock;

    public StockEntry create(Long productId, BigDecimal quantity, BigDecimal price, LocalDate expirationDate, LocalDate entryDate) {
        validatePositiveQuantity(quantity);
        validatePrice(price);
        if (entryDate == null) {
            throw new IllegalArgumentException("Entry date is required");
        }
        productRepository.findById(productId);
        return stockRepository.create(productId, StockEntry.builder()
                .quantity(quantity)
                .price(scale(price))
                .expirationDate(expirationDate)
                .entryDate(entryDate)
                .build(), StockMovementType.ENTRY, entryDate);
    }

    public StockEntry update(Long stockEntryId, BigDecimal quantity, BigDecimal price, LocalDate expirationDate, LocalDate entryDate) {
        validatePositiveQuantity(quantity);
        validatePrice(price);
        if (entryDate == null) {
            throw new IllegalArgumentException("Entry date is required");
        }
        return stockRepository.update(stockEntryId, StockEntry.builder()
                .quantity(quantity)
                .price(scale(price))
                .expirationDate(expirationDate)
                .entryDate(entryDate)
                .build(), StockMovementType.ADJUSTMENT, LocalDate.now(clock));
    }

    public StockEntry addQuantity(Long stockEntryId, BigDecimal quantity) {
        validatePositiveQuantity(quantity);
        return stockRepository.addQuantity(stockEntryId, quantity, StockMovementType.ADJUSTMENT, LocalDate.now(clock));
    }

    public void removeQuantity(Long stockEntryId, BigDecimal quantity) {
        validatePositiveQuantity(quantity);
        stockRepository.removeQuantity(stockEntryId, quantity, StockMovementType.ADJUSTMENT, LocalDate.now(clock));
    }

    public List<StockEntry> findStock(LocalDate expiresBefore, Collection<Long> productIds) {
        return stockRepository.findStock(expiresBefore, productIds);
    }

    public List<StockEntry> findStockByProduct(Long productId, LocalDate expiresBefore) {
        productRepository.findById(productId);
        return stockRepository.findStock(expiresBefore, List.of(productId));
    }

    public PageResult<StockMovement> findMovements(
            PaginationRequest pagination,
            LocalDate fromDate,
            LocalDate toDate,
            Collection<Long> productIds
    ) {
        validateDateRange(fromDate, toDate);
        List<StockMovement> items = stockMovementRepository.findPage(
                pagination.offset(),
                pagination.size(),
                fromDate,
                toDate,
                productIds
        );
        return new PageResult<>(items, pagination.page(), pagination.size(), stockMovementRepository.count(fromDate, toDate, productIds));
    }

    public StockReconciliationResponse reconcileProduct(Long productId) {
        var product = productRepository.findById(productId);
        StockMovementTotals totals = stockMovementRepository.summarizeByProduct(productId);
        BigDecimal liveQuantity = stockRepository.findStock(null, List.of(productId)).stream()
                .map(StockEntry::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal calculatedQuantity = totals.calculatedQuantity();
        BigDecimal difference = liveQuantity.subtract(calculatedQuantity).setScale(2, RoundingMode.HALF_UP);
        return new StockReconciliationResponse(
                productId,
                product.getName(),
                calculatedQuantity,
                liveQuantity,
                difference,
                totals.totalIn(),
                totals.totalOut(),
                totals.movementCount()
        );
    }

    private void validatePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to zero");
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(PRICE_SCALE, java.math.RoundingMode.HALF_UP);
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Movement start date must not be after movement end date");
        }
    }
}
