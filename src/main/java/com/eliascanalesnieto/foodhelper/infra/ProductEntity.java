package com.eliascanalesnieto.foodhelper.infra;

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
        @Column("media_id") Long mediaId
) {
}
