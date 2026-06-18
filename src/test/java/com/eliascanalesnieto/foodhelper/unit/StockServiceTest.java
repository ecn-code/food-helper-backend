package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.StockService;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockService service;

    @Test
    void shouldCreateStockEntryForExistingProduct() {
        when(productRepository.findById(1L)).thenReturn(Product.builder().id(1L).name("Apple").description("Fresh apple").build());
        ArgumentCaptor<StockEntry> stockEntryCaptor = forClass(StockEntry.class);
        when(stockRepository.create(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(StockEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        service.create(1L, new BigDecimal("3.5"), new BigDecimal("4.99"), LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 10));

        verify(stockRepository).create(org.mockito.ArgumentMatchers.eq(1L), stockEntryCaptor.capture());
        assertThat(stockEntryCaptor.getValue().getPrice()).isEqualByComparingTo("4.99");
    }

    @Test
    void shouldRejectZeroQuantityOnCreate() {
        assertThatThrownBy(() -> service.create(1L, BigDecimal.ZERO, BigDecimal.ONE, null, LocalDate.of(2026, 6, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectMissingEntryDateOnCreate() {
        assertThatThrownBy(() -> service.create(1L, BigDecimal.ONE, BigDecimal.ONE, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entry date is required");
    }

    @Test
    void shouldRejectZeroQuantityOnRemove() {
        assertThatThrownBy(() -> service.removeQuantity(5L, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }
}
