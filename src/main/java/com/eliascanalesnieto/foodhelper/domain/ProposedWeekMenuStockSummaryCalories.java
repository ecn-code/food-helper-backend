package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuStockSummaryCalories {
    BigDecimal averagePerPlannedDay;
    ProposedWeekMenuStockSummaryDayCalories maxDay;
    ProposedWeekMenuStockSummaryDayCalories minDay;
}
