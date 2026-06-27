package com.eliascanalesnieto.foodhelper.infra;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("supermarkets")
public record SupermarketEntity(
        @Id Long id,
        @Column("name") String name
) {
}
