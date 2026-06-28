package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuStatsService;
import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStatsRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuUsedStock;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMenuHistoryRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.time.Clock;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentWeekMenuHistoryServiceTest {
    @Mock ProposedWeekMenuService proposedWeekMenuService;
    @Mock CurrentWeekMenuRepository menuRepository;
    @Mock CurrentWeekMenuStatsRepository statsRepository;
    @Mock CurrentWeekMenuStatsService statsService;
    @Mock ProductRepository productRepository;
    @Mock StockRepository stockRepository;
    @Mock SupermarketRepository supermarketRepository;
    @Mock AppUserRepository userRepository;
    @Mock UserMoneyRepository moneyRepository;
    @Mock UserMenuHistoryRepository historyRepository;
    @Mock CurrentWeekMenuApiMapper mapper;
    @Mock NutritionalRulesService nutritionalRulesService;
    @Mock Clock clock;
    @InjectMocks CurrentWeekMenuService service;

    @Test
    void shouldSaveAnImmutableMenuSnapshotForEverySelectedPersonWhenClosing() {
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(), null, null, List.of(), List.of(), null
        );
        AppUser personOne = AppUser.builder().id(1L).username("one").build();
        AppUser personTwo = AppUser.builder().id(2L).username("two").build();
        CurrentWeekMenuStatsResponse stats = new CurrentWeekMenuStatsResponse(10L, null, null);

        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(clock.instant()).thenReturn(Instant.parse("2026-06-08T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(userRepository.findById(1L)).thenReturn(personOne);
        when(userRepository.findById(2L)).thenReturn(personTwo);
        when(statsRepository.findClosedWeekMenusByMonth(java.time.YearMonth.of(2026, 6))).thenReturn(List.of());
        when(statsService.build(menu, List.of(menu))).thenReturn(stats);
        when(statsRepository.save(stats)).thenReturn(stats);

        assertThat(service.close(10L, List.of(1L, 2L))).isSameAs(stats);
        verify(historyRepository).save(10L, personOne, menu);
        verify(historyRepository).save(10L, personTwo, menu);
    }

    @Test
    void repeatedCloseShouldReturnSavedStatsWithoutCreatingMoreSnapshots() {
        CurrentWeekMenuStatsResponse stats = new CurrentWeekMenuStatsResponse(10L, null, null);
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenReturn(stats);

        assertThat(service.close(10L, List.of(1L, 2L))).isSameAs(stats);

        verifyNoInteractions(historyRepository, userRepository);
        verify(menuRepository, never()).findById(10L);
    }

    @Test
    void closeShouldRejectAnEmptyPersonSelection() {
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(), null, null, List.of(), List.of(), null
        );
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(clock.instant()).thenReturn(Instant.parse("2026-06-08T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        assertThatThrownBy(() -> service.close(10L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one person must be selected");
    }

    @Test
    void undoShouldRestoreExactStockDeleteMoneyMovementAndMenu() {
        CurrentWeekMenuUsedStockResponse usedStock = new CurrentWeekMenuUsedStockResponse(
                7L, 5L, "Rice", new BigDecimal("1.25"), new BigDecimal("2.00"),
                new BigDecimal("2.50"), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 1)
        );
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(), null, null, List.of(usedStock), List.of(), null
        );
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));

        service.undo(10L);

        ArgumentCaptor<CurrentWeekMenuUsedStock> restored = ArgumentCaptor.forClass(CurrentWeekMenuUsedStock.class);
        verify(stockRepository).restore(restored.capture());
        assertThat(restored.getValue().getStockEntryId()).isEqualTo(7L);
        assertThat(restored.getValue().getUsedUnits()).isEqualByComparingTo("1.25");
        assertThat(restored.getValue().getPrice()).isEqualByComparingTo("2.00");
        verify(moneyRepository).deleteMovementsByCurrentWeekMenuId(10L);
        verify(menuRepository).delete(10L);
    }
}
