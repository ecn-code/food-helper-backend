package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecipeDerivedProduct {
    Long productId;
    BigDecimal producedGrams;
    BigDecimal gramsPerUnit;
    BigDecimal unitsProduced;
}
