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
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
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
    void shouldRejectOverlappingProposedMenusWhenCreatingANewOne() {
        when(menuRepository.findAllSummaries()).thenReturn(List.of(
                new PlanningSummary(1L, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21), 7, PlanningState.DRAFT, null)
        ));

        assertThatThrownBy(() -> service.create(LocalDate.of(2026, 6, 21), LocalDate.of(2026, 6, 27)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Planning overlaps an existing menu");

        verify(menuRepository, never()).create(org.mockito.Mockito.any());
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
}
