package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
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

    @InjectMocks
    private ProposedWeekMenuService service;

    @Test
    void shouldRejectProposedMenusLongerThanAWeekRange() {
        assertThatThrownBy(() -> service.create(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 23)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Proposed week menu cannot span more than 8 days");
    }

    @Test
    void shouldRejectRepeatedProductSortOrdersWithinTheSameSection() {
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
}
