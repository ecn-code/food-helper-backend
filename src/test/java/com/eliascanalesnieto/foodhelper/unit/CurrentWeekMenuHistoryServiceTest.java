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
import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStatsRepository;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuUsedStock;
import com.eliascanalesnieto.foodhelper.domain.MenuStockMovement;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.MenuStockMovementRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.domain.StockMovementType;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMenuHistoryRepository;
import com.eliascanalesnieto.foodhelper.domain.UserMoneyRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuRecipeProductionResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateMenuStockMovementRequest;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.time.Clock;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
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
    @Mock RecipeRepository recipeRepository;
    @Mock StockRepository stockRepository;
    @Mock SupermarketRepository supermarketRepository;
    @Mock AppUserRepository userRepository;
    @Mock UserMoneyRepository moneyRepository;
    @Mock MenuStockMovementRepository menuStockMovementRepository;
    @Mock UserMenuHistoryRepository historyRepository;
    @Mock PlanningCouponService planningCouponService;
    @Mock CurrentWeekMenuApiMapper mapper;
    @Mock NutritionalRulesService nutritionalRulesService;
    @Mock Clock clock;
    @InjectMocks CurrentWeekMenuService service;

    @Test
    void shouldSaveAnImmutableMenuSnapshotForEverySelectedPersonWhenClosing() {
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(), null, null, List.of(), List.of(), List.of(), List.of(), null
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
    void establishFromProposedShouldRejectOverlappingMenusForTheSameUser() {
        CurrentWeekMenuResponse overlappingMenu = new CurrentWeekMenuResponse(
                11L, 21L, 1L, "payer",
                List.of(),
                LocalDate.of(2026, 6, 18), LocalDate.of(2026, 6, 24),
                List.of(), null, null, List.of(), List.of(), List.of(), List.of(), null
        );
        AppUser payer = AppUser.builder().id(1L).username("payer").build();
        com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu proposedWeekMenu = com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu.builder()
                .id(20L)
                .startDate(LocalDate.of(2026, 6, 20))
                .endDate(LocalDate.of(2026, 6, 26))
                .days(List.of())
                .build();

        when(menuRepository.findByProposedWeekMenuId(20L)).thenThrow(new ResourceNotFoundException("not established"));
        when(userRepository.findById(1L)).thenReturn(payer);
        when(proposedWeekMenuService.findById(20L)).thenReturn(proposedWeekMenu);
        when(menuRepository.findAll()).thenReturn(List.of(overlappingMenu));

        assertThatThrownBy(() -> service.establishFromProposed(20L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User already has an overlapping menu");
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
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(), null, null, List.of(), List.of(), List.of(), List.of(), null
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
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(), null, null, List.of(usedStock), List.of(), List.of(), List.of(), null
        );
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(clock.instant()).thenReturn(Instant.parse("2026-06-08T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        service.undo(10L);

        ArgumentCaptor<CurrentWeekMenuUsedStock> restored = ArgumentCaptor.forClass(CurrentWeekMenuUsedStock.class);
        verify(stockRepository).restore(
                (CurrentWeekMenuUsedStock) restored.capture(),
                org.mockito.ArgumentMatchers.eq(StockMovementType.ADJUSTMENT),
                org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 6, 8))
        );
        assertThat(restored.getValue().getStockEntryId()).isEqualTo(7L);
        assertThat(restored.getValue().getUsedUnits()).isEqualByComparingTo("1.25");
        assertThat(restored.getValue().getPrice()).isEqualByComparingTo("2.00");
        verify(menuStockMovementRepository).deleteByCurrentWeekMenuId(10L);
        verify(moneyRepository).deleteMovementsByCurrentWeekMenuId(10L);
        verify(menuRepository).delete(10L);
    }

    @Test
    void addStockMovementShouldUsePayerByDefaultAndUpdateShoppingList() {
        AppUser payer = AppUser.builder().id(1L).username("payer").build();
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(),
                null,
                null,
                List.of(),
                List.of(new com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuShoppingListItemResponse(
                        99L, "Rice", new BigDecimal("2.00")
                )),
                List.of(),
                List.of(),
                null
        );
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(userRepository.findById(1L)).thenReturn(payer);
        when(productRepository.findById(99L)).thenReturn(com.eliascanalesnieto.foodhelper.domain.Product.builder().id(99L).name("Rice").build());
        when(clock.instant()).thenReturn(Instant.parse("2026-06-02T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(menuRepository.save(org.mockito.ArgumentMatchers.any(CurrentWeekMenuResponse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponseStockMovements(org.mockito.ArgumentMatchers.anyList())).thenAnswer(invocation -> {
            MenuStockMovement movement = ((List<MenuStockMovement>) invocation.getArgument(0)).getFirst();
            return List.of(new MenuStockMovementResponse(
                    movement.getId(),
                    movement.getCurrentWeekMenuId(),
                    movement.getUserId(),
                    movement.getUserUsername(),
                    movement.getProductId(),
                    movement.getProductName(),
                    movement.getQuantity(),
                    movement.getPrice(),
                    movement.getTotalCost(),
                    movement.getDescription(),
                    movement.getCreatedAt()
            ));
        });
        when(menuStockMovementRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            MenuStockMovement movement = invocation.getArgument(0);
            return movement.toBuilder().id(50L).createdAt(LocalDateTime.of(2026, 6, 2, 10, 0)).build();
        });

        CurrentWeekMenuResponse updated = service.addStockMovement(
                10L,
                new CreateMenuStockMovementRequest(null, 99L, new BigDecimal("1.00"), new BigDecimal("2.50"), "Weekly groceries")
        );

        verify(moneyRepository).addMovement(1L, new BigDecimal("-2.50"), "Weekly groceries", 10L);
        verify(menuRepository).save(org.mockito.ArgumentMatchers.any(CurrentWeekMenuResponse.class));
        assertThat(updated.shoppingList()).singleElement().satisfies(item ->
                assertThat(item.missingUnits()).isEqualByComparingTo("1.00")
        );
        assertThat(updated.stockMovements()).singleElement().satisfies(item -> {
            assertThat(item.userId()).isEqualTo(1L);
            assertThat(item.productId()).isEqualTo(99L);
            assertThat(item.totalCost()).isEqualByComparingTo("2.50");
        });
    }

    @Test
    void updateResponsibleShouldChangeTheDefaultUserForOpenMenu() {
        AppUser payer = AppUser.builder().id(2L).username("new-payer").build();
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(),
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null
        );
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(userRepository.findById(2L)).thenReturn(payer);
        when(menuRepository.save(org.mockito.ArgumentMatchers.any(CurrentWeekMenuResponse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CurrentWeekMenuResponse updated = service.updateResponsible(10L, 2L);

        assertThat(updated.payerUserId()).isEqualTo(2L);
        assertThat(updated.payerUsername()).isEqualTo("new-payer");
        verify(menuRepository).save(org.mockito.ArgumentMatchers.any(CurrentWeekMenuResponse.class));
    }

    @Test
    void transferRecipeProductionShouldCreateStockAndPersistTrace() {
        CurrentWeekMenuRecipeProductionResponse production = new CurrentWeekMenuRecipeProductionResponse(
                91L,
                77L,
                "Recipe",
                55L,
                "Derived Product",
                new BigDecimal("4.00"),
                1,
                false,
                null,
                null,
                null
        );
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(new com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse(
                        5L,
                        LocalDate.of(2026, 6, 3),
                        List.of(),
                        List.of(new com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuRecipeProductionResponse(
                                91L, 77L, "Recipe", 55L, "Derived Product", new BigDecimal("4.00"), 1
                        )),
                        new com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse(
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                        )
                )),
                new com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                ),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(production),
                null
        );
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(productRepository.findById(55L)).thenReturn(com.eliascanalesnieto.foodhelper.domain.Product.builder().id(55L).name("Derived Product").build());
        when(stockRepository.create(
                org.mockito.ArgumentMatchers.eq(55L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(StockMovementType.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        ))
                .thenReturn(com.eliascanalesnieto.foodhelper.domain.StockEntry.builder().id(123L).productId(55L).quantity(new BigDecimal("4.00")).price(BigDecimal.ZERO).build());
        when(clock.instant()).thenReturn(Instant.parse("2026-06-03T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        CurrentWeekMenuResponse updated = service.transferRecipeProduction(10L, 91L);

        assertThat(updated.recipeProductions()).singleElement().satisfies(item -> {
            assertThat(item.transferred()).isTrue();
            assertThat(item.transferType()).isEqualTo("MANUAL");
            assertThat(item.stockEntryId()).isEqualTo(123L);
        });
        ArgumentCaptor<CurrentWeekMenuResponse> persisted = ArgumentCaptor.forClass(CurrentWeekMenuResponse.class);
        verify(menuRepository).save(persisted.capture());
        assertThat(persisted.getValue().recipeProductions()).singleElement().satisfies(item ->
                assertThat(item.transferred()).isTrue()
        );
    }

    @Test
    void closeShouldAutomaticallyTransferPendingRecipeProductions() {
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                10L, 20L, 1L, "payer",
                List.of(),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7),
                List.of(new com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayResponse(
                        5L,
                        LocalDate.of(2026, 6, 3),
                        List.of(),
                        List.of(new com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuRecipeProductionResponse(
                                91L, 77L, "Recipe", 55L, "Derived Product", new BigDecimal("4.00"), 1
                        )),
                        new com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse(
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                        )
                )),
                new com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
                ),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(new CurrentWeekMenuRecipeProductionResponse(
                        91L, 77L, "Recipe", 55L, "Derived Product",
                        new BigDecimal("4.00"), 1,
                        false, null, null, null
                )),
                null
        );
        AppUser person = AppUser.builder().id(1L).username("one").build();
        CurrentWeekMenuStatsResponse stats = new CurrentWeekMenuStatsResponse(10L, null, null);

        when(statsRepository.findByCurrentWeekMenuId(10L)).thenThrow(new ResourceNotFoundException("not closed"));
        when(menuRepository.findById(10L)).thenReturn(menu);
        when(clock.instant()).thenReturn(Instant.parse("2026-06-08T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(userRepository.findById(1L)).thenReturn(person);
        when(statsRepository.findClosedWeekMenusByMonth(java.time.YearMonth.of(2026, 6))).thenReturn(List.of());
        when(statsService.build(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(stats);
        when(statsRepository.save(stats)).thenReturn(stats);
        when(productRepository.findById(55L)).thenReturn(com.eliascanalesnieto.foodhelper.domain.Product.builder().id(55L).name("Derived Product").build());
        when(stockRepository.create(
                org.mockito.ArgumentMatchers.eq(55L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(StockMovementType.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        ))
                .thenReturn(com.eliascanalesnieto.foodhelper.domain.StockEntry.builder().id(123L).productId(55L).quantity(new BigDecimal("4.00")).price(BigDecimal.ZERO).build());

        assertThat(service.close(10L, List.of(1L))).isSameAs(stats);

        ArgumentCaptor<CurrentWeekMenuResponse> persisted = ArgumentCaptor.forClass(CurrentWeekMenuResponse.class);
        verify(menuRepository).save(persisted.capture());
        assertThat(persisted.getValue().recipeProductions()).singleElement().satisfies(item ->
                assertThat(item.transferred()).isTrue()
        );
        verify(historyRepository).save(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(person), org.mockito.ArgumentMatchers.eq(persisted.getValue()));
    }
}
