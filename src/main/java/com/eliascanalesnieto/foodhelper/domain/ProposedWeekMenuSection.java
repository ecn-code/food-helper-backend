package com.eliascanalesnieto.foodhelper.domain;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuSection {
    Long id;
    String name;
    Integer sortOrder;
    List<ProposedWeekMenuProduct> products;
    NutritionalValues nutritionalValues;
}
