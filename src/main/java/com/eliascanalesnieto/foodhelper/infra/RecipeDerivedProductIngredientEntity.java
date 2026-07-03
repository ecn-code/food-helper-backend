package com.eliascanalesnieto.foodhelper.infra;

import java.math.BigDecimal;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("recipe_derived_product_ingredients")
public record RecipeDerivedProductIngredientEntity(
        @Column("recipe_id") Long recipeId,
        @Column("product_id") Long productId,
        @Column("quantity") BigDecimal quantity,
        @Column("quantity_type") String quantityType
) {
}
