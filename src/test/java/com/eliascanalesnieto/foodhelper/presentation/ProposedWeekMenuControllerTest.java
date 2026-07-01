package com.eliascanalesnieto.foodhelper.presentation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProposedWeekMenuControllerTest {
    private final ProposedWeekMenuService service = mock(ProposedWeekMenuService.class);
    private final CurrentWeekMenuService currentWeekMenuService = mock(CurrentWeekMenuService.class);
    private final ProposedWeekMenuApiMapper mapper = mock(ProposedWeekMenuApiMapper.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new ProposedWeekMenuController(service, currentWeekMenuService, mapper)
    ).build();

    @Test
    void deleteShouldForwardToTheServiceAndReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/planning/42"))
                .andExpect(status().isNoContent());

        verify(service).delete(42L);
    }
}
