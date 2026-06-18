package com.eliascanalesnieto.foodhelper.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ProposedWeekMenuDay {
    Long id;
    LocalDate date;
    List<ProposedWeekMenuSection> sections;
    NutritionalValues nutritionalValues;
}
