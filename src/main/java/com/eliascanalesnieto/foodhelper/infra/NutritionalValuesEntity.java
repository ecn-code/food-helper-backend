package com.eliascanalesnieto.foodhelper.infra;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("nutritional_values")
public record NutritionalValuesEntity(
        @Id @Column("product_id") Long productId,
        @Column("calories") BigDecimal calories,
        @Column("carbohydrates") BigDecimal carbohydrates,
        @Column("proteins") BigDecimal proteins,
        @Column("fats") BigDecimal fats
) {
}
