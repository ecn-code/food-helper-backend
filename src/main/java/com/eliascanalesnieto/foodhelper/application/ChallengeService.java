package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.ChallengeRedemption;
import com.eliascanalesnieto.foodhelper.domain.ChallengeRedemptionRepository;
import com.eliascanalesnieto.foodhelper.domain.ChallengeDefinition;
import com.eliascanalesnieto.foodhelper.domain.ChallengeDefinitionRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.ChallengeResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRedemptionRepository redemptionRepository;
    private final UserMoneyRepository userMoneyRepository;
    private final AppUserRepository appUserRepository;
    private final ChallengeDefinitionRepository challengeDefinitionRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ChallengeResponse> findChallenges(Long payerUserId, boolean onlyAvailable) {
        appUserRepository.findById(payerUserId);
        Instant now = Instant.now(clock);
        return challengeDefinitionRepository.findAll().stream()
                .map(challenge -> evaluate(challenge, payerUserId, now))
                .filter(challenge -> !onlyAvailable || challenge.available())
                .toList();
    }

    @Transactional
    public ChallengeResponse redeemChallenge(Long payerUserId, String challengeCode) {
        appUserRepository.findById(payerUserId);
        ChallengeDefinition challenge = findChallenge(challengeCode);
        Instant now = Instant.now(clock);
        ChallengeResponse evaluation = evaluate(challenge, payerUserId, now);
        if (!evaluation.available()) {
            throw new IllegalArgumentException("Challenge " + challenge.getCode() + " is not available");
        }
        redemptionRepository.save(ChallengeRedemption.builder()
                .userId(payerUserId)
                .challengeCode(challenge.getCode())
                .rewardAmount(challenge.getRewardAmount())
                .usedAt(now)
                .build());
        userMoneyRepository.addMovement(payerUserId, challenge.getRewardAmount(), "Challenge " + challenge.getCode());
        return evaluate(challenge, payerUserId, now);
    }

    @Transactional
    public ChallengeDefinition createDefinition(ChallengeDefinition challenge) { return challengeDefinitionRepository.create(normalize(challenge)); }
    @Transactional
    public ChallengeDefinition updateDefinition(String code, ChallengeDefinition challenge) { return challengeDefinitionRepository.update(normalizeCode(code), normalize(challenge)); }
    @Transactional
    public void deleteDefinition(String code) { challengeDefinitionRepository.delete(normalizeCode(code)); }

    private ChallengeResponse evaluate(ChallengeDefinition challenge, Long userId, Instant now) {
        ChallengeRedemption lastRedemption = redemptionRepository
                .findLatestByUserIdAndChallengeCode(userId, challenge.getCode())
                .orElse(null);
        Instant lastUsedAt = lastRedemption == null ? null : lastRedemption.getUsedAt();
        Instant nextAvailableAt = lastUsedAt == null ? null : lastUsedAt.plus(challenge.getPeriodDays(), ChronoUnit.DAYS);
        boolean available = nextAvailableAt == null || !nextAvailableAt.isAfter(now);
        return new ChallengeResponse(
                challenge.getId(), challenge.getCode(), challenge.getName(), challenge.getDescription(), challenge.getRewardAmount(), challenge.getPeriodDays(),
                available, lastUsedAt, available ? null : nextAvailableAt);
    }

    private ChallengeDefinition findChallenge(String challengeCode) {
        if (challengeCode == null || challengeCode.isBlank()) {
            throw new IllegalArgumentException("Challenge code is required");
        }
        return challengeDefinitionRepository.findByCode(normalizeCode(challengeCode));
    }
    private ChallengeDefinition normalize(ChallengeDefinition challenge) { return challenge.toBuilder().code(normalizeCode(challenge.getCode())).name(challenge.getName().trim()).description(challenge.getDescription().trim()).rewardAmount(challenge.getRewardAmount().setScale(2, java.math.RoundingMode.HALF_UP)).build(); }
    private String normalizeCode(String code) { if (code == null || code.isBlank()) throw new IllegalArgumentException("Challenge code is required"); return code.trim().toUpperCase(Locale.ROOT); }
}
