package com.eliascanalesnieto.foodhelper.infra;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Table("products")
public record ProductEntity(
        @Id Long id,
        @Column("name") String name,
        @Column("description") String description,
        @Column("grams_per_unit") BigDecimal gramsPerUnit,
        @Column("is_stock_in_units") boolean stockInUnits,
        @Column("nutrition_basis") String nutritionBasis,
        @Column("default_price") BigDecimal defaultPrice,
        @Column("media_id") Long mediaId,
        @Column("created_at") Instant createdAt
) {
}
