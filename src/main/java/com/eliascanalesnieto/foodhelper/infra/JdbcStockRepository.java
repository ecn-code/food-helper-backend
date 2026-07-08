package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockMovement;
import com.eliascanalesnieto.foodhelper.domain.StockMovementRepository;
import com.eliascanalesnieto.foodhelper.domain.StockMovementType;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuUsedStock;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Types;
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
    private final StockMovementRepository stockMovementRepository;
    private final Clock clock;

    @Override
    @Transactional
    public StockEntry create(Long productId, StockEntry stockEntry, StockMovementType movementType, LocalDate effectiveDate) {
        StockEntryEntity savedEntry = stockCrudRepository.save(new StockEntryEntity(
                null,
                productId,
                stockEntry.getQuantity(),
                stockEntry.getPrice(),
                stockEntry.getExpirationDate(),
                stockEntry.getEntryDate()
        ));
        recordMovement(savedEntry.id(), productId, stockEntry.getQuantity(), movementType, effectiveDate, stockEntry.getPrice(), stockEntry.getExpirationDate(), stockEntry.getEntryDate());
        return findStockEntry(savedEntry.id());
    }

    @Override
    public StockEntry findById(Long stockEntryId) {
        return findStockEntry(stockEntryId);
    }

    @Override
    @Transactional
    public StockEntry update(Long stockEntryId, StockEntry stockEntry, StockMovementType movementType, LocalDate effectiveDate) {
        StockEntryEntity existingEntry = stockCrudRepository.findById(stockEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
        stockCrudRepository.save(new StockEntryEntity(
                existingEntry.id(),
                existingEntry.productId(),
                stockEntry.getQuantity(),
                stockEntry.getPrice(),
                stockEntry.getExpirationDate(),
                stockEntry.getEntryDate()
        ));
        BigDecimal quantityDelta = stockEntry.getQuantity().subtract(existingEntry.quantity());
        if (quantityDelta.signum() != 0) {
            recordMovement(
                    existingEntry.id(),
                    existingEntry.productId(),
                    quantityDelta,
                    movementType,
                    effectiveDate,
                    stockEntry.getPrice(),
                    stockEntry.getExpirationDate(),
                    stockEntry.getEntryDate()
            );
        }
        return findStockEntry(stockEntryId);
    }

    @Override
    @Transactional
    public StockEntry addQuantity(Long stockEntryId, BigDecimal quantity, StockMovementType movementType, LocalDate effectiveDate) {
        StockEntryEntity existingEntry = stockCrudRepository.findById(stockEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
        stockCrudRepository.save(new StockEntryEntity(
                existingEntry.id(),
                existingEntry.productId(),
                existingEntry.quantity().add(quantity),
                existingEntry.price(),
                existingEntry.expirationDate(),
                existingEntry.entryDate()
        ));
        recordMovement(stockEntryId, existingEntry.productId(), quantity, movementType, effectiveDate, existingEntry.price(), existingEntry.expirationDate(), existingEntry.entryDate());
        return findStockEntry(stockEntryId);
    }

    @Override
    @Transactional
    public void removeQuantity(Long stockEntryId, BigDecimal quantity, StockMovementType movementType, LocalDate effectiveDate) {
        StockEntryEntity existingEntry = stockCrudRepository.findById(stockEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
        BigDecimal updatedQuantity = existingEntry.quantity().subtract(quantity);
        if (updatedQuantity.signum() < 0) {
            throw new IllegalArgumentException("Quantity to remove exceeds current stock");
        }
        BigDecimal signedQuantity = quantity.negate();
        if (updatedQuantity.signum() == 0) {
            stockCrudRepository.deleteById(stockEntryId);
            recordMovement(stockEntryId, existingEntry.productId(), signedQuantity, movementType, effectiveDate, existingEntry.price(), existingEntry.expirationDate(), existingEntry.entryDate());
            return;
        }
        stockCrudRepository.save(new StockEntryEntity(
                existingEntry.id(),
                existingEntry.productId(),
                updatedQuantity,
                existingEntry.price(),
                existingEntry.expirationDate(),
                existingEntry.entryDate()
        ));
        recordMovement(stockEntryId, existingEntry.productId(), signedQuantity, movementType, effectiveDate, existingEntry.price(), existingEntry.expirationDate(), existingEntry.entryDate());
    }

    @Override
    @Transactional
    public void delete(Long stockEntryId, StockMovementType movementType, LocalDate effectiveDate) {
        if (!stockCrudRepository.existsById(stockEntryId)) {
            throw new ResourceNotFoundException("Stock entry not found");
        }
        StockEntryEntity existingEntry = stockCrudRepository.findById(stockEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
        recordMovement(stockEntryId, existingEntry.productId(), existingEntry.quantity().negate(), movementType, effectiveDate, existingEntry.price(), existingEntry.expirationDate(), existingEntry.entryDate());
        stockCrudRepository.deleteById(stockEntryId);
    }

    @Override
    @Transactional
    public void restore(CurrentWeekMenuUsedStock usedStock, StockMovementType movementType, LocalDate effectiveDate) {
        jdbcClient.sql("""
                INSERT INTO stock_entries (id, product_id, quantity, price, expiration_date, entry_date)
                VALUES (:id, :productId, :quantity, :price, :expirationDate, :entryDate)
                ON CONFLICT (id) DO UPDATE
                SET quantity = stock_entries.quantity + EXCLUDED.quantity
                """)
                .param("id", usedStock.getStockEntryId())
                .param("productId", usedStock.getProductId())
                .param("quantity", usedStock.getUsedUnits())
                .param("price", usedStock.getPrice())
                .param("expirationDate", usedStock.getExpirationDate(), Types.DATE)
                .param("entryDate", usedStock.getEntryDate())
                .update();
        recordMovement(
                usedStock.getStockEntryId(),
                usedStock.getProductId(),
                usedStock.getUsedUnits(),
                movementType,
                effectiveDate,
                usedStock.getPrice(),
                usedStock.getExpirationDate(),
                usedStock.getEntryDate(),
                usedStock.getProductName()
        );
    }

    @Override
    public List<StockEntry> findStock(LocalDate expiresBefore, Collection<Long> productIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.product_id,
                       p.name AS product_name,
                       s.quantity,
                       s.price,
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
                        .price(rs.getBigDecimal("price"))
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
                       s.price,
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
                        .price(rs.getBigDecimal("price"))
                        .expirationDate(rs.getObject("expiration_date", LocalDate.class))
                        .entryDate(rs.getObject("entry_date", LocalDate.class))
                        .build())
                .optional()
                        .orElseThrow(() -> new ResourceNotFoundException("Stock entry not found"));
    }

    private void recordMovement(
            Long stockEntryId,
            Long productId,
            BigDecimal signedQuantity,
            StockMovementType movementType,
            LocalDate effectiveDate,
            BigDecimal price,
            LocalDate expirationDate,
            LocalDate entryDate
    ) {
        recordMovement(stockEntryId, productId, signedQuantity, movementType, effectiveDate, price, expirationDate, entryDate, findProductName(productId));
    }

    private void recordMovement(
            Long stockEntryId,
            Long productId,
            BigDecimal signedQuantity,
            StockMovementType movementType,
            LocalDate effectiveDate,
            BigDecimal price,
            LocalDate expirationDate,
            LocalDate entryDate,
            String productName
    ) {
        stockMovementRepository.record(StockMovement.builder()
                .stockEntryId(stockEntryId)
                .productId(productId)
                .productName(productName)
                .movementType(movementType)
                .signedQuantity(signedQuantity)
                .effectiveDate(effectiveDate)
                .recordedAt(LocalDateTime.now(clock))
                .price(price)
                .expirationDate(expirationDate)
                .entryDate(entryDate)
                .build());
    }

    private String findProductName(Long productId) {
        return jdbcClient.sql("""
                SELECT name
                FROM products
                WHERE id = :productId
                """)
                .param("productId", productId)
                .query(String.class)
                .single();
    }
}
