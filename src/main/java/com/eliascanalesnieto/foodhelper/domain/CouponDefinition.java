package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CouponDefinition {
    Long id;
    String code;
    String name;
    String conditionDescription;
    String ruleCode;
    BigDecimal rewardAmount;
    int periodDays;
}
