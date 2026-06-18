package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProposedWeekMenuDayPart {
    Long id;
    String name;
    String description;
    Integer sortOrder;
}
