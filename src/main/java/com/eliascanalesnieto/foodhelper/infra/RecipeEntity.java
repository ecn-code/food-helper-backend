package com.eliascanalesnieto.foodhelper.infra;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("recipes")
public record RecipeEntity(
        @Id Long id,
        @Column("name") String name,
        @Column("description") String description,
        @Column("instructions") String instructions
) {
}
