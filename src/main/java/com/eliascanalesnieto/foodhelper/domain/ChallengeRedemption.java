package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChallengeRedemption {
    Long id;
    Long userId;
    String challengeCode;
    BigDecimal rewardAmount;
    Instant usedAt;
}
