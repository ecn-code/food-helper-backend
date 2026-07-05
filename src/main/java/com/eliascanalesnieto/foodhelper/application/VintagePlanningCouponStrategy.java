package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.UserMenuHistoryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class VintagePlanningCouponStrategy implements PlanningCouponStrategy {
    private static final BigDecimal REWARD_AMOUNT = new BigDecimal("5.00");
    private static final int PERIOD_DAYS = 30;

    private final UserMenuHistoryRepository userMenuHistoryRepository;

    public VintagePlanningCouponStrategy(UserMenuHistoryRepository userMenuHistoryRepository) {
        this.userMenuHistoryRepository = userMenuHistoryRepository;
    }

    @Override
    public String code() {
        return "VINTAGE";
    }

    @Override
    public String name() {
        return "Vintage";
    }

    @Override
    public String conditionDescription() {
        return "The menu must include a product not used in the last two months";
    }

    @Override
    public int periodDays() {
        return PERIOD_DAYS;
    }

    @Override
    public BigDecimal rewardAmount() {
        return REWARD_AMOUNT;
    }

    @Override
    public boolean matches(ProposedWeekMenu proposedMenu) {
        return false;
    }

    @Override
    public boolean matches(ProposedWeekMenu proposedMenu, Long payerUserId, Instant now) {
        Set<Long> currentProductIds = PlanningCouponMenuInspector.productIdsFrom(proposedMenu);
        if (currentProductIds.isEmpty()) {
            return false;
        }
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        Set<Long> historicalProductIds = userMenuHistoryRepository.findMenus(
                payerUserId,
                today.minusMonths(2),
                today
        ).stream()
                .map(PlanningCouponMenuInspector::productIdsFrom)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return currentProductIds.stream().anyMatch(productId -> !historicalProductIds.contains(productId));
    }
}
