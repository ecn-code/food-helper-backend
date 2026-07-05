package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class InnovationPlanningCouponStrategy implements PlanningCouponStrategy {
    private static final BigDecimal REWARD_AMOUNT = new BigDecimal("15.00");
    private static final int PERIOD_DAYS = 30;

    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;

    public InnovationPlanningCouponStrategy(ProductRepository productRepository, RecipeRepository recipeRepository) {
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public String code() {
        return "INOVACION";
    }

    @Override
    public String name() {
        return "Innovacion";
    }

    @Override
    public String conditionDescription() {
        return "The menu must include a recipe-derived product created less than one month ago";
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
        List<Product> products = productRepository.findByIds(currentProductIds);
        Instant cutoff = now.minus(30, ChronoUnit.DAYS);
        return products.stream()
                .filter(product -> recipeRepository.findDerivedProductByProductId(product.getId()).isPresent())
                .map(Product::getCreatedAt)
                .filter(java.util.Objects::nonNull)
                .anyMatch(createdAt -> createdAt.isAfter(cutoff));
    }
}
