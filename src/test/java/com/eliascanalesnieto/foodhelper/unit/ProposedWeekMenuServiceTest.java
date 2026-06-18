package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import java.time.LocalDate;
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
    void shouldRejectProposedMenusLongerThanAWeek() {
        assertThatThrownBy(() -> service.create(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 22)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Proposed week menu cannot span more than 7 days");
    }
}
