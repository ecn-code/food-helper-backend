package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

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
                .build());
    }

    public StockEntry addQuantity(Long stockEntryId, BigDecimal quantity) {
        validatePositiveQuantity(quantity);
        return stockRepository.addQuantity(stockEntryId, quantity);
    }

    public void removeQuantity(Long stockEntryId, BigDecimal quantity) {
        validatePositiveQuantity(quantity);
        stockRepository.removeQuantity(stockEntryId, quantity);
    }

    public List<StockEntry> findStock(LocalDate expiresBefore, Collection<Long> productIds) {
        return stockRepository.findStock(expiresBefore, productIds);
    }

    public List<StockEntry> findStockByProduct(Long productId, LocalDate expiresBefore) {
        productRepository.findById(productId);
        return stockRepository.findStock(expiresBefore, List.of(productId));
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
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
