package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuStockRequirement {
    Long productId;
    String productName;
    BigDecimal requiredUnits;
    BigDecimal availableUnits;
    BigDecimal coveredUnits;
    BigDecimal missingUnits;
    BigDecimal estimatedCost;
}
