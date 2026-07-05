package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.InnovationPlanningCouponStrategy;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InnovationPlanningCouponStrategyTest {
    private final ProductRepository productRepository = Mockito.mock(ProductRepository.class);
    private final RecipeRepository recipeRepository = Mockito.mock(RecipeRepository.class);
    private final InnovationPlanningCouponStrategy strategy = new InnovationPlanningCouponStrategy(productRepository, recipeRepository);

    @Test
    void shouldAllowMenuWhenItIncludesARecentRecipeDerivedProduct() {
        ProposedWeekMenu menu = menu(day("2026-07-05", section(1L, product(10L), product(11L))));
        when(productRepository.findByIds(anyCollection())).thenReturn(List.of(
                derivedProduct(10L, Instant.parse("2026-06-20T10:00:00Z")),
                regularProduct(11L, Instant.parse("2026-06-20T10:00:00Z"))
        ));
        when(recipeRepository.findDerivedProductByProductId(10L)).thenReturn(java.util.Optional.of(recipeProduct(10L)));
        when(recipeRepository.findDerivedProductByProductId(11L)).thenReturn(java.util.Optional.empty());

        assertThat(strategy.matches(menu, 7L, Instant.parse("2026-07-05T10:00:00Z"))).isTrue();
    }

    @Test
    void shouldRejectMenuWhenTheDerivedProductIsTooOld() {
        ProposedWeekMenu menu = menu(day("2026-07-05", section(1L, product(10L))));
        when(productRepository.findByIds(anyCollection())).thenReturn(List.of(
                derivedProduct(10L, Instant.parse("2026-05-01T10:00:00Z"))
        ));
        when(recipeRepository.findDerivedProductByProductId(10L)).thenReturn(java.util.Optional.of(recipeProduct(10L)));

        assertThat(strategy.matches(menu, 7L, Instant.parse("2026-07-05T10:00:00Z"))).isFalse();
    }

    @Test
    void shouldRejectMenuWhenItHasNoRecipeDerivedProducts() {
        ProposedWeekMenu menu = menu(day("2026-07-05", section(1L, product(10L))));
        when(productRepository.findByIds(anyCollection())).thenReturn(List.of(
                regularProduct(10L, Instant.parse("2026-06-20T10:00:00Z"))
        ));
        when(recipeRepository.findDerivedProductByProductId(10L)).thenReturn(java.util.Optional.empty());

        assertThat(strategy.matches(menu, 7L, Instant.parse("2026-07-05T10:00:00Z"))).isFalse();
    }

    private Product derivedProduct(Long id, Instant createdAt) {
        return baseProduct(id, createdAt).build();
    }

    private Product regularProduct(Long id, Instant createdAt) {
        return baseProduct(id, createdAt).build();
    }

    private Product.ProductBuilder baseProduct(Long id, Instant createdAt) {
        return Product.builder()
                .id(id)
                .name("Product " + id)
                .description("Description " + id)
                .gramsPerUnit(new BigDecimal("100.00"))
                .createdAt(createdAt);
    }

    private RecipeDerivedProduct recipeProduct(Long id) {
        return RecipeDerivedProduct.builder()
                .productId(id)
                .name("Derived " + id)
                .unitsProduced(new BigDecimal("1.00"))
                .stockFromComposition(true)
                .ingredients(List.of())
                .build();
    }

    private ProposedWeekMenu menu(ProposedWeekMenuDay... days) {
        return ProposedWeekMenu.builder()
                .days(List.of(days))
                .build();
    }

    private ProposedWeekMenuDay day(String date, ProposedWeekMenuSection... sections) {
        return ProposedWeekMenuDay.builder()
                .date(LocalDate.parse(date))
                .sections(List.of(sections))
                .build();
    }

    private ProposedWeekMenuSection section(Long dayPartId, ProposedWeekMenuProduct... products) {
        return ProposedWeekMenuSection.builder()
                .dayPartId(dayPartId)
                .products(List.of(products))
                .build();
    }

    private ProposedWeekMenuProduct product(Long productId) {
        return ProposedWeekMenuProduct.builder()
                .productId(productId)
                .units(BigDecimal.ONE)
                .grams(BigDecimal.ONE)
                .sortOrder(10)
                .build();
    }
}
