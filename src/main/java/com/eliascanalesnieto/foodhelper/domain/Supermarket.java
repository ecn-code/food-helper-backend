package com.eliascanalesnieto.foodhelper.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Supermarket {
    Long id;
    String name;
}
