package com.eliascanalesnieto.foodhelper.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ProposedWeekMenu {
    Long id;
    Integer users;
    LocalDate startDate;
    LocalDate endDate;
    List<ProposedWeekMenuDay> days;
    NutritionalValues nutritionalValues;
    ProposedWeekMenuStockSummary stockSummary;
}
