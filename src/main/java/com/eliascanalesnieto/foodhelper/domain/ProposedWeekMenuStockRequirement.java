package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ProposedWeekMenuStockRequirement {
    Long productId;
    String productName;
    BigDecimal requiredUnits;
    BigDecimal availableUnits;
    BigDecimal coveredUnits;
    BigDecimal missingUnits;
    BigDecimal estimatedCost;
}
