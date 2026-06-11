package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcStockRepository implements StockRepository {
    private final StockCrudRepository stockCrudRepository;
    private final JdbcClient jdbcClient;

    @Override
    @Transactional
    public StockEntry create(Long productId, StockEntry stockEntry) {
        StockEntryEntity savedEntry = stockCrudRepository.save(new StockEntryEntity(
                null,
                productId,
                stockEntry.getQuantity(),
                stockEntry.getExpirationDate(),
                stockEntry.getEntryDate()
        ));
        return findStockEntry(savedEntry.id());
    }

    @Override
    @Transactional
    public StockEntry addQuantity(Long stockEntryId, BigDecimal quantity) {
        StockEntryEntity existingEntry = stockCrudRepository.findById(stockEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
        stockCrudRepository.save(new StockEntryEntity(
                existingEntry.id(),
                existingEntry.productId(),
                existingEntry.quantity().add(quantity),
                existingEntry.expirationDate(),
                existingEntry.entryDate()
        ));
        return findStockEntry(stockEntryId);
    }

    @Override
    @Transactional
    public void removeQuantity(Long stockEntryId, BigDecimal quantity) {
        StockEntryEntity existingEntry = stockCrudRepository.findById(stockEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
        BigDecimal updatedQuantity = existingEntry.quantity().subtract(quantity);
        if (updatedQuantity.signum() < 0) {
            throw new IllegalArgumentException("Quantity to remove exceeds current stock");
        }
        if (updatedQuantity.signum() == 0) {
            stockCrudRepository.deleteById(stockEntryId);
            return;
        }
        stockCrudRepository.save(new StockEntryEntity(
                existingEntry.id(),
                existingEntry.productId(),
                updatedQuantity,
                existingEntry.expirationDate(),
                existingEntry.entryDate()
        ));
    }

    @Override
    public List<StockEntry> findStock(LocalDate expiresBefore, Collection<Long> productIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.product_id,
                       p.name AS product_name,
                       s.quantity,
                       s.expiration_date,
                       s.entry_date
                FROM stock_entries s
                INNER JOIN products p ON p.id = s.product_id
                WHERE 1 = 1
                """);
        Map<String, Object> params = new HashMap<>();

        if (expiresBefore != null) {
            sql.append(" AND s.expiration_date IS NOT NULL AND s.expiration_date < :expiresBefore");
            params.put("expiresBefore", expiresBefore);
        }
        if (productIds != null && !productIds.isEmpty()) {
            sql.append(" AND s.product_id IN (:productIds)");
            params.put("productIds", productIds);
        }

        sql.append("""
                 ORDER BY
                     CASE WHEN s.expiration_date IS NULL THEN 1 ELSE 0 END,
                     s.expiration_date ASC,
                     s.entry_date ASC,
                     s.id ASC
                """);

        return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> StockEntry.builder()
                        .id(rs.getLong("id"))
                        .productId(rs.getLong("product_id"))
                        .productName(rs.getString("product_name"))
                        .quantity(rs.getBigDecimal("quantity"))
                        .expirationDate(rs.getObject("expiration_date", LocalDate.class))
                        .entryDate(rs.getObject("entry_date", LocalDate.class))
                        .build())
                .list();
    }

    private StockEntry findStockEntry(Long stockEntryId) {
        return jdbcClient.sql("""
                SELECT s.id,
                       s.product_id,
                       p.name AS product_name,
                       s.quantity,
                       s.expiration_date,
                       s.entry_date
                FROM stock_entries s
                INNER JOIN products p ON p.id = s.product_id
                WHERE s.id = :stockEntryId
                """)
                .param("stockEntryId", stockEntryId)
                .query((rs, rowNum) -> StockEntry.builder()
                        .id(rs.getLong("id"))
                        .productId(rs.getLong("product_id"))
                        .productName(rs.getString("product_name"))
                        .quantity(rs.getBigDecimal("quantity"))
                        .expirationDate(rs.getObject("expiration_date", LocalDate.class))
                        .entryDate(rs.getObject("entry_date", LocalDate.class))
                        .build())
                .optional()
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
    }
}
