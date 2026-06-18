package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ProposedWeekMenuSection {
    Long id;
    Long dayPartId;
    String name;
    String description;
    Integer sortOrder;
    List<ProposedWeekMenuProduct> products;
    NutritionalValues nutritionalValues;
}
