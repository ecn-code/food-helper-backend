package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemption;
import com.eliascanalesnieto.foodhelper.domain.PlanningCouponRedemptionRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanningCouponService {
    private final ProposedWeekMenuService proposedWeekMenuService;
    private final PlanningCouponRedemptionRepository redemptionRepository;
    private final UserMoneyRepository userMoneyRepository;
    private final AppUserRepository appUserRepository;
    private final List<PlanningCouponStrategy> strategies;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<PlanningCouponResponse> findCoupons(Long proposedWeekMenuId, Long payerUserId) {
        ProposedWeekMenu proposedMenu = proposedWeekMenuService.findById(proposedWeekMenuId);
        return findCoupons(proposedMenu, payerUserId);
    }

    @Transactional(readOnly = true)
    public List<PlanningCouponResponse> findCoupons(ProposedWeekMenu proposedMenu, Long payerUserId) {
        appUserRepository.findById(payerUserId);
        Instant now = Instant.now(clock);
        Map<String, PlanningCouponStrategy> strategiesByCode = strategiesByCode();
        Map<String, PlanningCouponRedemption> latestRedemptions = loadLatestRedemptions(payerUserId, strategiesByCode.keySet());
        return strategies.stream()
                .sorted(java.util.Comparator.comparing(PlanningCouponStrategy::code))
                .map(strategy -> strategy.evaluate(proposedMenu, latestRedemptions.get(strategy.code()), now))
                .toList();
    }

    @Transactional
    public void redeemCoupons(
            ProposedWeekMenu proposedMenu,
            Long payerUserId,
            Long currentWeekMenuId,
            List<String> requestedCouponCodes
    ) {
        appUserRepository.findById(payerUserId);
        List<String> normalizedCouponCodes = normalizeCodes(requestedCouponCodes);
        if (normalizedCouponCodes.isEmpty()) {
            return;
        }

        Instant now = Instant.now(clock);
        Map<String, PlanningCouponStrategy> strategiesByCode = strategiesByCode();
        Map<String, PlanningCouponRedemption> latestRedemptions = loadLatestRedemptions(payerUserId, strategiesByCode.keySet());
        Map<String, PlanningCouponResponse> evaluationsByCode = new LinkedHashMap<>();
        for (String couponCode : normalizedCouponCodes) {
            PlanningCouponStrategy strategy = strategiesByCode.get(couponCode);
            if (strategy == null) {
                throw new IllegalArgumentException("Unknown coupon code: " + couponCode);
            }
            PlanningCouponResponse evaluation = strategy.evaluate(proposedMenu, latestRedemptions.get(couponCode), now);
            if (!evaluation.available()) {
                throw new IllegalArgumentException("Coupon " + couponCode + " is not available");
            }
            evaluationsByCode.put(couponCode, evaluation);
        }

        for (PlanningCouponResponse evaluation : evaluationsByCode.values()) {
            redeemCoupon(payerUserId, proposedMenu.getId(), currentWeekMenuId, evaluation, now);
        }
    }

    @Transactional
    public void deleteRedemptionsByCurrentWeekMenuId(Long currentWeekMenuId) {
        redemptionRepository.deleteByCurrentWeekMenuId(currentWeekMenuId);
    }

    private void redeemCoupon(
            Long userId,
            Long planningId,
            Long currentWeekMenuId,
            PlanningCouponResponse evaluation,
            Instant now
    ) {
        redemptionRepository.save(PlanningCouponRedemption.builder()
                .userId(userId)
                .couponCode(evaluation.code())
                .planningId(planningId)
                .currentWeekMenuId(currentWeekMenuId)
                .rewardAmount(evaluation.rewardAmount())
                .usedAt(now)
                .build());
        userMoneyRepository.addMovement(
                userId,
                evaluation.rewardAmount(),
                "Coupon " + evaluation.code() + " #" + currentWeekMenuId,
                currentWeekMenuId
        );
    }

    private Map<String, PlanningCouponStrategy> strategiesByCode() {
        Map<String, PlanningCouponStrategy> strategiesByCode = new LinkedHashMap<>();
        for (PlanningCouponStrategy strategy : strategies) {
            strategiesByCode.put(strategy.code().toUpperCase(Locale.ROOT), strategy);
        }
        return strategiesByCode;
    }

    private Map<String, PlanningCouponRedemption> loadLatestRedemptions(Long userId, Set<String> couponCodes) {
        Map<String, PlanningCouponRedemption> redemptions = new LinkedHashMap<>();
        for (String couponCode : couponCodes) {
            Optional<PlanningCouponRedemption> redemption = redemptionRepository.findLatestByUserIdAndCouponCode(userId, couponCode);
            redemption.ifPresent(value -> redemptions.put(couponCode, value));
        }
        return redemptions;
    }

    private List<String> normalizeCodes(List<String> requestedCouponCodes) {
        if (requestedCouponCodes == null || requestedCouponCodes.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String couponCode : requestedCouponCodes) {
            if (couponCode == null || couponCode.isBlank()) {
                throw new IllegalArgumentException("Coupon code is required");
            }
            String normalizedCode = couponCode.trim().toUpperCase(Locale.ROOT);
            if (!normalized.add(normalizedCode)) {
                throw new IllegalArgumentException("Duplicate coupon code: " + normalizedCode);
            }
        }
        return new ArrayList<>(normalized);
    }
}
