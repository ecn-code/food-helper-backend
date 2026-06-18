package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuStockSummary {
    Integer plannedDays;
    Integer distinctProducts;
    ProposedWeekMenuStockSummaryCalories calories;
    BigDecimal estimatedCost;
    List<ProposedWeekMenuStockRequirement> requirements;
}
