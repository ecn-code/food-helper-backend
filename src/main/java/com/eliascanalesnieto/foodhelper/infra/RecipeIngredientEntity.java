package com.eliascanalesnieto.foodhelper.infra;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("recipe_products")
public record RecipeIngredientEntity(
        @Id Long id,
        @Column("recipe_id") Long recipeId,
        @Column("product_id") Long productId,
        @Column("grams") BigDecimal grams
) {
}
