package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStatsRepository;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.PlanningState;
import com.eliascanalesnieto.foodhelper.domain.PlanningSummary;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProposedWeekMenuServiceTest {

    @Mock
    private ProposedWeekMenuRepository menuRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CurrentWeekMenuStatsRepository currentWeekMenuStatsRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProposedWeekMenuService service;

    @Test
    void shouldRejectProposedMenusLongerThanTwoWeeksRange() {
        assertThatThrownBy(() -> service.create(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Planning cannot span more than 16 days");
    }

    @Test
    void shouldCreateProposedMenusEvenWhenOtherSummariesAlreadyExist() {
        when(menuRepository.create(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            ProposedWeekMenu menu = invocation.getArgument(0);
            return menu.toBuilder()
                    .id(99L)
                    .build();
        });

        ProposedWeekMenu created = service.create(LocalDate.of(2026, 6, 21), LocalDate.of(2026, 6, 27));

        assertThat(created.getId()).isEqualTo(99L);
        assertThat(created.getDays()).isEmpty();
        verify(menuRepository).create(org.mockito.Mockito.any());
    }

    @Test
    void shouldStoreTheRequestedUserCountWhenCreatingAPlanning() {
        when(menuRepository.create(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProposedWeekMenu created = service.create(LocalDate.of(2026, 6, 21), LocalDate.of(2026, 6, 27), 3);

        assertThat(created.getUsers()).isEqualTo(3);
        verify(menuRepository).create(org.mockito.ArgumentMatchers.argThat(menu -> menu.getUsers().equals(3)));
    }

    @Test
    void shouldRejectRepeatedProductSortOrdersWithinTheSameSection() {
        when(currentWeekMenuStatsRepository.findByProposedWeekMenuId(1L))
                .thenThrow(new ResourceNotFoundException("Established week menu not found"));

        ProposedWeekMenuDay day = ProposedWeekMenuDay.builder()
                .date(LocalDate.of(2026, 6, 15))
                .sections(List.of(
                        ProposedWeekMenuSection.builder()
                                .dayPartId(1L)
                                .products(List.of(
                                        ProposedWeekMenuProduct.builder().productId(10L).sortOrder(10).units(BigDecimal.ONE).build(),
                                        ProposedWeekMenuProduct.builder().productId(11L).sortOrder(10).units(BigDecimal.ONE).build()
                                ))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> service.upsertDay(1L, day))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product sortOrder must be unique within each section");
    }

    @Test
    void shouldRejectManualProductsWithoutRequiredNutrition() {
        when(currentWeekMenuStatsRepository.findByProposedWeekMenuId(1L))
                .thenThrow(new ResourceNotFoundException("Established week menu not found"));

        ProposedWeekMenuDay day = ProposedWeekMenuDay.builder()
                .date(LocalDate.of(2026, 6, 15))
                .sections(List.of(
                        ProposedWeekMenuSection.builder()
                                .dayPartId(1L)
                                .products(List.of(
                                        ProposedWeekMenuProduct.builder()
                                                .productName("Homemade bowl")
                                                .sortOrder(10)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> service.upsertDay(1L, day))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Manual products require absolute calories, carbohydrates, proteins, and fats values");
    }

    @Test
    void shouldRejectManualProductsThatIncludeQuantity() {
        when(currentWeekMenuStatsRepository.findByProposedWeekMenuId(1L))
                .thenThrow(new ResourceNotFoundException("Established week menu not found"));

        ProposedWeekMenuDay day = ProposedWeekMenuDay.builder()
                .date(LocalDate.of(2026, 6, 15))
                .sections(List.of(
                        ProposedWeekMenuSection.builder()
                                .dayPartId(1L)
                                .products(List.of(
                                        ProposedWeekMenuProduct.builder()
                                                .productName("Homemade bowl")
                                                .units(new BigDecimal("1"))
                                                .nutritionalValues(com.eliascanalesnieto.foodhelper.domain.NutritionalValues.builder()
                                                        .calories(new BigDecimal("180"))
                                                        .carbohydrates(new BigDecimal("24"))
                                                        .proteins(new BigDecimal("5"))
                                                        .fats(new BigDecimal("6"))
                                                        .build())
                                                .sortOrder(10)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> service.upsertDay(1L, day))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Manual products must not include units or grams");
    }

    @Test
    void shouldRejectLinkedProductsThatAlsoSendManualData() {
        when(currentWeekMenuStatsRepository.findByProposedWeekMenuId(1L))
                .thenThrow(new ResourceNotFoundException("Established week menu not found"));

        ProposedWeekMenuDay day = ProposedWeekMenuDay.builder()
                .date(LocalDate.of(2026, 6, 15))
                .sections(List.of(
                        ProposedWeekMenuSection.builder()
                                .dayPartId(1L)
                                .products(List.of(
                                        ProposedWeekMenuProduct.builder()
                                                .productId(10L)
                                                .productName("Mixed payload")
                                                .nutritionalValues(com.eliascanalesnieto.foodhelper.domain.NutritionalValues.builder()
                                                        .calories(new BigDecimal("10"))
                                                        .carbohydrates(new BigDecimal("1"))
                                                        .proteins(new BigDecimal("2"))
                                                        .fats(new BigDecimal("3"))
                                                        .build())
                                                .sortOrder(10)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> service.upsertDay(1L, day))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Linked products must not include manual product data");
    }

    @Test
    void shouldAllowManualProductsWithoutCatalogLookups() {
        when(currentWeekMenuStatsRepository.findByProposedWeekMenuId(1L))
                .thenThrow(new ResourceNotFoundException("Established week menu not found"));
        when(menuRepository.upsertDay(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    ProposedWeekMenuDay completedDay = invocation.getArgument(1);
                    return ProposedWeekMenu.builder()
                            .id(1L)
                            .startDate(LocalDate.of(2026, 6, 15))
                            .endDate(LocalDate.of(2026, 6, 21))
                            .days(List.of(completedDay))
                            .build();
                });

        ProposedWeekMenuDay day = ProposedWeekMenuDay.builder()
                .date(LocalDate.of(2026, 6, 15))
                .sections(List.of(
                        ProposedWeekMenuSection.builder()
                                .dayPartId(1L)
                                .products(List.of(
                                        ProposedWeekMenuProduct.builder()
                                                .productName("Homemade bowl")
                                                .nutritionalValues(com.eliascanalesnieto.foodhelper.domain.NutritionalValues.builder()
                                                        .calories(new BigDecimal("180"))
                                                        .carbohydrates(new BigDecimal("24"))
                                                        .proteins(new BigDecimal("5"))
                                                        .fats(new BigDecimal("6"))
                                                        .build())
                                                .sortOrder(10)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        ProposedWeekMenu result = service.upsertDay(1L, day);

        assertThat(result.getDays()).hasSize(1);
        assertThat(result.getDays().getFirst().getSections().getFirst().getProducts().getFirst().getUnits())
                .isNull();
        assertThat(result.getDays().getFirst().getSections().getFirst().getProducts().getFirst().getNutritionalValues().getCalories())
                .isEqualByComparingTo("180.00");
        assertThat(result.getDays().getFirst().getSections().getFirst().getProducts().getFirst().getGrams())
                .isNull();
        assertThat(result.getStockSummary().getDistinctProducts()).isZero();
        verify(productRepository, never()).findByIds(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void shouldExposeDraftEstablishedAndClosedPlanningStatesFromCatalog() {
        List<PlanningSummary> summaries = List.of(
                new PlanningSummary(3L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 7), 0, PlanningState.DRAFT, null),
                new PlanningSummary(2L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7), 4, PlanningState.ESTABLISHED, 20L),
                new PlanningSummary(1L, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 7, PlanningState.CLOSED, 10L)
        );
        when(menuRepository.findAllSummaries()).thenReturn(summaries);

        assertThat(service.findAllSummaries()).containsExactlyElementsOf(summaries);
        assertThat(service.findAllSummaries()).extracting(PlanningSummary::state)
                .containsExactly(PlanningState.DRAFT, PlanningState.ESTABLISHED, PlanningState.CLOSED);
    }

    @Test
    void shouldDeletePlanningThroughTheRepository() {
        service.delete(42L);

        verify(menuRepository).delete(42L);
    }

    @Test
    void shouldExpandCompositionDerivedProductsIntoIngredientRequirements() {
        when(currentWeekMenuStatsRepository.findByProposedWeekMenuId(1L))
                .thenThrow(new ResourceNotFoundException("Established week menu not found"));

        RecipeDerivedProduct derivedProduct = RecipeDerivedProduct.builder()
                .productId(302L)
                .name("Albondigas de calabaza y queso")
                .unitsProduced(new BigDecimal("2.00"))
                .stockFromComposition(true)
                .ingredients(List.of(
                        RecipeIngredient.builder()
                                .productId(401L)
                                .productName("Planning Pumpkin")
                                .quantity(new BigDecimal("100.00"))
                                .quantityType(QuantityType.GRAMS)
                                .build(),
                        RecipeIngredient.builder()
                                .productId(402L)
                                .productName("Planning Cheese")
                                .quantity(new BigDecimal("25.00"))
                                .quantityType(QuantityType.GRAMS)
                                .build()
                ))
                .build();

        when(productRepository.findByIds(org.mockito.ArgumentMatchers.anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return ids.stream()
                    .map(id -> switch (id.intValue()) {
                        case 302 -> product(302L, "Albondigas de calabaza y queso", "255");
                        case 401 -> product(401L, "Planning Pumpkin", "100");
                        case 402 -> product(402L, "Planning Cheese", "100");
                        default -> null;
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        });
        when(recipeRepository.findDerivedProductByProductId(302L)).thenReturn(java.util.Optional.of(derivedProduct));
        when(stockRepository.findStock(null, null)).thenReturn(List.of(
                stockEntry(11L, 401L, "Planning Pumpkin", "100.00", LocalDate.of(2026, 6, 10)),
                stockEntry(12L, 402L, "Planning Cheese", "25.00", LocalDate.of(2026, 6, 10))
        ));
        when(menuRepository.upsertDay(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    ProposedWeekMenuDay completedDay = invocation.getArgument(1);
                    return ProposedWeekMenu.builder()
                            .id(1L)
                            .users(1)
                            .startDate(LocalDate.of(2026, 6, 15))
                            .endDate(LocalDate.of(2026, 6, 21))
                            .days(List.of(completedDay))
                            .build();
                });

        ProposedWeekMenuDay day = ProposedWeekMenuDay.builder()
                .date(LocalDate.of(2026, 6, 15))
                .sections(List.of(
                        ProposedWeekMenuSection.builder()
                                .dayPartId(1L)
                                .products(List.of(
                                        ProposedWeekMenuProduct.builder()
                                                .productId(302L)
                                                .units(new BigDecimal("1.00"))
                                                .grams(new BigDecimal("255.00"))
                                                .sortOrder(10)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        ProposedWeekMenu planned = service.upsertDay(1L, day);

        assertThat(planned.getStockSummary().getDistinctProducts()).isEqualTo(2);
        assertThat(planned.getStockSummary().getRequirements())
                .filteredOn(requirement -> requirement.getProductId().equals(401L))
                .singleElement()
                .satisfies(requirement -> {
                    assertThat(requirement.getProductName()).isEqualTo("Planning Pumpkin");
                    assertThat(requirement.getRequiredUnits()).isEqualByComparingTo("100.00");
                    assertThat(requirement.getAvailableUnits()).isEqualByComparingTo("100.00");
                    assertThat(requirement.getCoveredUnits()).isEqualByComparingTo("100.00");
                    assertThat(requirement.getMissingUnits()).isEqualByComparingTo("0.00");
                    assertThat(requirement.getEstimatedCost()).isEqualByComparingTo("200.00");
                });
        assertThat(planned.getStockSummary().getRequirements())
                .filteredOn(requirement -> requirement.getProductId().equals(402L))
                .singleElement()
                .satisfies(requirement -> {
                    assertThat(requirement.getProductName()).isEqualTo("Planning Cheese");
                    assertThat(requirement.getRequiredUnits()).isEqualByComparingTo("25.00");
                    assertThat(requirement.getAvailableUnits()).isEqualByComparingTo("25.00");
                    assertThat(requirement.getCoveredUnits()).isEqualByComparingTo("25.00");
                    assertThat(requirement.getMissingUnits()).isEqualByComparingTo("0.00");
                    assertThat(requirement.getEstimatedCost()).isEqualByComparingTo("50.00");
                });
    }

    private com.eliascanalesnieto.foodhelper.domain.Product product(Long id, String name, String gramsPerUnit) {
        return com.eliascanalesnieto.foodhelper.domain.Product.builder()
                .id(id)
                .name(name)
                .gramsPerUnit(new BigDecimal(gramsPerUnit))
                .nutritionalValues(NutritionalValues.builder()
                        .calories(BigDecimal.ZERO)
                        .carbohydrates(BigDecimal.ZERO)
                        .proteins(BigDecimal.ZERO)
                        .fats(BigDecimal.ZERO)
                        .build())
                .build();
    }

    private StockEntry stockEntry(Long id, Long productId, String productName, String quantity, LocalDate entryDate) {
        return StockEntry.builder()
                .id(id)
                .productId(productId)
                .productName(productName)
                .quantity(new BigDecimal(quantity))
                .price(new BigDecimal("2.00"))
                .expirationDate(null)
                .entryDate(entryDate)
                .build();
    }
}
