package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.StockMovement;
import com.eliascanalesnieto.foodhelper.domain.StockMovementRepository;
import com.eliascanalesnieto.foodhelper.domain.StockMovementTotals;
import com.eliascanalesnieto.foodhelper.domain.StockMovementType;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcStockMovementRepository implements StockMovementRepository {
    private static final int SCALE = 2;
    private static final int PRICE_SCALE = 4;

    private final JdbcTemplate jdbcTemplate;
    private final JdbcClient jdbcClient;

    @Override
    public StockMovement record(StockMovement movement) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO stock_movements (
                        product_id,
                        stock_entry_id,
                        movement_type,
                        signed_quantity,
                        effective_date,
                        recorded_at,
                        product_name,
                        price,
                        expiration_date,
                        entry_date
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            ps.setLong(1, movement.getProductId());
            if (movement.getStockEntryId() == null) {
                ps.setNull(2, java.sql.Types.BIGINT);
            } else {
                ps.setLong(2, movement.getStockEntryId());
            }
            ps.setString(3, movement.getMovementType().name());
            ps.setBigDecimal(4, scale(movement.getSignedQuantity()));
            ps.setObject(5, movement.getEffectiveDate());
            ps.setObject(6, movement.getRecordedAt());
            ps.setString(7, movement.getProductName());
            if (movement.getPrice() == null) {
                ps.setNull(8, java.sql.Types.NUMERIC);
            } else {
                ps.setBigDecimal(8, scalePrice(movement.getPrice()));
            }
            if (movement.getExpirationDate() == null) {
                ps.setNull(9, java.sql.Types.DATE);
            } else {
                ps.setObject(9, movement.getExpirationDate());
            }
            if (movement.getEntryDate() == null) {
                ps.setNull(10, java.sql.Types.DATE);
            } else {
                ps.setObject(10, movement.getEntryDate());
            }
            return ps;
        }, keyHolder);
        return movement.toBuilder().id(keyHolder.getKey().longValue()).build();
    }

    @Override
    public List<StockMovement> findPage(int offset, int limit, LocalDate fromDate, LocalDate toDate, Collection<Long> productIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       product_id,
                       stock_entry_id,
                       movement_type,
                       signed_quantity,
                       effective_date,
                       recorded_at,
                       product_name,
                       price,
                       expiration_date,
                       entry_date
                FROM stock_movements
                WHERE 1 = 1
                """);
        Map<String, Object> params = new HashMap<>();
        appendFilters(sql, params, fromDate, toDate, productIds);
        sql.append("""
                 ORDER BY effective_date DESC, recorded_at DESC, id DESC
                 LIMIT :limit OFFSET :offset
                """);
        params.put("limit", limit);
        params.put("offset", offset);
        return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    @Override
    public long count(LocalDate fromDate, LocalDate toDate, Collection<Long> productIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) AS total
                FROM stock_movements
                WHERE 1 = 1
                """);
        Map<String, Object> params = new HashMap<>();
        appendFilters(sql, params, fromDate, toDate, productIds);
        return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> rs.getLong("total"))
                .single();
    }

    @Override
    public StockMovementTotals summarizeByProduct(Long productId) {
        return jdbcClient.sql("""
                SELECT
                    COALESCE(SUM(signed_quantity), 0) AS calculated_quantity,
                    COALESCE(SUM(CASE WHEN signed_quantity > 0 THEN signed_quantity ELSE 0 END), 0) AS total_in,
                    COALESCE(SUM(CASE WHEN signed_quantity < 0 THEN -signed_quantity ELSE 0 END), 0) AS total_out,
                    COUNT(*) AS movement_count
                FROM stock_movements
                WHERE product_id = :productId
                """)
                .param("productId", productId)
                .query((rs, rowNum) -> new StockMovementTotals(
                        rs.getBigDecimal("calculated_quantity").setScale(SCALE, java.math.RoundingMode.HALF_UP),
                        rs.getBigDecimal("total_in").setScale(SCALE, java.math.RoundingMode.HALF_UP),
                        rs.getBigDecimal("total_out").setScale(SCALE, java.math.RoundingMode.HALF_UP),
                        rs.getLong("movement_count")
                ))
                .single();
    }

    private void appendFilters(
            StringBuilder sql,
            Map<String, Object> params,
            LocalDate fromDate,
            LocalDate toDate,
            Collection<Long> productIds
    ) {
        if (fromDate != null) {
            sql.append(" AND effective_date >= :fromDate");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            sql.append(" AND effective_date <= :toDate");
            params.put("toDate", toDate);
        }
        if (productIds != null && !productIds.isEmpty()) {
            sql.append(" AND product_id IN (:productIds)");
            params.put("productIds", productIds);
        }
    }

    private StockMovement mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return StockMovement.builder()
                .id(rs.getLong("id"))
                .productId(rs.getLong("product_id"))
                .stockEntryId(rs.getObject("stock_entry_id", Long.class))
                .movementType(StockMovementType.valueOf(rs.getString("movement_type")))
                .signedQuantity(rs.getBigDecimal("signed_quantity"))
                .effectiveDate(rs.getObject("effective_date", LocalDate.class))
                .recordedAt(rs.getTimestamp("recorded_at").toLocalDateTime())
                .productName(rs.getString("product_name"))
                .price(rs.getBigDecimal("price"))
                .expirationDate(rs.getObject("expiration_date", LocalDate.class))
                .entryDate(rs.getObject("entry_date", LocalDate.class))
                .build();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal scalePrice(BigDecimal value) {
        return value.setScale(PRICE_SCALE, java.math.RoundingMode.HALF_UP);
    }
}
