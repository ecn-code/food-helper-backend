package com.eliascanalesnieto.foodhelper.infra;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("stock_entries")
public record StockEntryEntity(
        @Id Long id,
        @Column("product_id") Long productId,
        @Column("quantity") BigDecimal quantity,
        @Column("expiration_date") LocalDate expirationDate,
        @Column("entry_date") LocalDate entryDate
) {
}
