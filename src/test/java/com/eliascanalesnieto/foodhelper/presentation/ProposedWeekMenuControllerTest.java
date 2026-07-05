package com.eliascanalesnieto.foodhelper.presentation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProposedWeekMenuControllerTest {
    private final ProposedWeekMenuService service = mock(ProposedWeekMenuService.class);
    private final CurrentWeekMenuService currentWeekMenuService = mock(CurrentWeekMenuService.class);
    private final PlanningCouponService planningCouponService = mock(PlanningCouponService.class);
    private final ProposedWeekMenuApiMapper mapper = mock(ProposedWeekMenuApiMapper.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new ProposedWeekMenuController(service, currentWeekMenuService, planningCouponService, mapper)
    ).build();

    @Test
    void deleteShouldForwardToTheServiceAndReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/planning/42"))
                .andExpect(status().isNoContent());

        verify(service).delete(42L);
    }

    @Test
    void findCouponsShouldForwardToTheCouponService() throws Exception {
        when(planningCouponService.findCoupons(42L, 7L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/planning/42/coupons")
                        .param("payerUserId", "7"))
                .andExpect(status().isOk());

        verify(planningCouponService).findCoupons(42L, 7L);
    }

    @Test
    void validateCouponsShouldForwardToTheCouponService() throws Exception {
        when(planningCouponService.validateCoupons(42L, 7L, List.of("NO_REPEATED_PRODUCTS")))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/v1/planning/42/coupons/validate")
                        .contentType("application/json")
                        .content("""
                                {"payerUserId":7,"couponCodes":["NO_REPEATED_PRODUCTS"]}
                                """))
                .andExpect(status().isOk());

        verify(planningCouponService).validateCoupons(42L, 7L, List.of("NO_REPEATED_PRODUCTS"));
    }
}
