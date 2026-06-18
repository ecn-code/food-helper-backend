package com.eliascanalesnieto.foodhelper.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenu {
    Long id;
    LocalDate startDate;
    LocalDate endDate;
    List<ProposedWeekMenuDay> days;
    NutritionalValues nutritionalValues;
    ProposedWeekMenuStockSummary stockSummary;
}
