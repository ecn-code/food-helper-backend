package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.domain.Supermarket;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuShoppingListItemResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentWeekMenuShoppingListServiceTest {
    @Mock
    private CurrentWeekMenuRepository currentWeekMenuRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private SupermarketRepository supermarketRepository;
    @Mock
    private StockRepository stockRepository;
    @InjectMocks
    private CurrentWeekMenuService service;

    @Test
    void shouldReturnSavedListAndFilterWithoutChangingStock() {
        CurrentWeekMenuShoppingListItemResponse rice = new CurrentWeekMenuShoppingListItemResponse(10L, "Rice", new BigDecimal("2.00"));
        CurrentWeekMenuShoppingListItemResponse beans = new CurrentWeekMenuShoppingListItemResponse(20L, "Beans", new BigDecimal("0.50"));
        CurrentWeekMenuResponse menu = new CurrentWeekMenuResponse(
                1L, 2L, 3L, "payer", null, null, List.of(), null, null, List.of(), List.of(rice, beans), null
        );
        when(currentWeekMenuRepository.findById(1L)).thenReturn(menu);

        assertThat(service.findShoppingList(1L, null)).containsExactly(rice, beans);

        when(supermarketRepository.findById(7L)).thenReturn(Supermarket.builder().id(7L).name("Market").build());
        when(productRepository.findProductIdsBySupermarket(7L, List.of(10L, 20L))).thenReturn(List.of(20L));
        assertThat(service.findShoppingList(1L, 7L)).containsExactly(beans);

        verify(supermarketRepository).findById(7L);
        verify(productRepository).findProductIdsBySupermarket(7L, List.of(10L, 20L));
        verifyNoInteractions(stockRepository);
    }
}
