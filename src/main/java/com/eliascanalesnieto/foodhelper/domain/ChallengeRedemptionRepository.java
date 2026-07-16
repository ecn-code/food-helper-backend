package com.eliascanalesnieto.foodhelper.domain;

import java.util.Optional;

public interface ChallengeRedemptionRepository {
    Optional<ChallengeRedemption> findLatestByUserIdAndChallengeCode(Long userId, String challengeCode);

    ChallengeRedemption save(ChallengeRedemption redemption);
}
