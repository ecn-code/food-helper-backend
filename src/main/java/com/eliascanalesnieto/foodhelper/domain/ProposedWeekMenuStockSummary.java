package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ProposedWeekMenuStockSummary {
    Integer plannedDays;
    Integer distinctProducts;
    ProposedWeekMenuStockSummaryCalories calories;
    BigDecimal estimatedCost;
    List<ProposedWeekMenuStockRequirement> requirements;
}
