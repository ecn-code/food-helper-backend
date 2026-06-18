package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuStockSummaryDayCalories {
    LocalDate date;
    BigDecimal calories;
}
