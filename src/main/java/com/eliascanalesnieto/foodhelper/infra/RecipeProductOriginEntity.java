package com.eliascanalesnieto.foodhelper.infra;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("recipe_product_origins")
public record RecipeProductOriginEntity(
        @Id @Column("recipe_id") Long recipeId,
        @Column("product_id") Long productId,
        @Column("units_produced") BigDecimal unitsProduced,
        @Column("stock_from_composition") boolean stockFromComposition
) {
}
