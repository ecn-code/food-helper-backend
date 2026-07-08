package com.eliascanalesnieto.foodhelper.presentation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CouponControllerTest {
    private final PlanningCouponService planningCouponService = mock(PlanningCouponService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new CouponController(planningCouponService)
    ).build();

    @Test
    void findCouponsShouldForwardToTheCouponService() throws Exception {
        when(planningCouponService.findGlobalCoupons(7L, true)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/coupons")
                        .param("payerUserId", "7")
                        .param("onlyAvailable", "true"))
                .andExpect(status().isOk());

        verify(planningCouponService).findGlobalCoupons(7L, true);
    }
}
