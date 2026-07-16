package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChallengeDefinition {
    Long id;
    String code;
    String name;
    String description;
    BigDecimal rewardAmount;
    int periodDays;
}
