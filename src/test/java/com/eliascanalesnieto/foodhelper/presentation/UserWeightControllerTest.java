package com.eliascanalesnieto.foodhelper.presentation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliascanalesnieto.foodhelper.application.UserWeightService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserWeightControllerTest {

    private final UserWeightService service = mock(UserWeightService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserWeightController(service)).build();

    @Test
    void createShouldAcceptNotesInTheJsonBody() throws Exception {
        when(service.create(
                eq(7L),
                eq(new BigDecimal("72.35")),
                eq(Instant.parse("2026-06-28T08:30:00Z")),
                eq("After breakfast")
        )).thenReturn(new UserWeightResponse(
                1L,
                7L,
                new BigDecimal("72.35"),
                Instant.parse("2026-06-28T08:30:00Z"),
                "After breakfast",
                Instant.parse("2026-06-28T08:30:00Z"),
                Instant.parse("2026-06-28T08:30:00Z")
        ));

        mockMvc.perform(post("/api/v1/users/7/weights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weight":72.35,"recordedAt":"2026-06-28T08:30:00Z","notes":"After breakfast"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("After breakfast"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-28T08:30:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-28T08:30:00Z"));

        verify(service).create(
                7L,
                new BigDecimal("72.35"),
                Instant.parse("2026-06-28T08:30:00Z"),
                "After breakfast"
        );
    }

    @Test
    void updateShouldAcceptNotesInTheJsonBody() throws Exception {
        when(service.update(
                eq(7L),
                eq(9L),
                eq(new BigDecimal("70.25")),
                eq(Instant.parse("2026-06-29T08:15:00Z")),
                eq("Edited note")
        )).thenReturn(new UserWeightResponse(
                9L,
                7L,
                new BigDecimal("70.25"),
                Instant.parse("2026-06-29T08:15:00Z"),
                "Edited note",
                Instant.parse("2026-06-28T08:30:00Z"),
                Instant.parse("2026-06-29T08:15:00Z")
        ));

        mockMvc.perform(put("/api/v1/users/7/weights/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weight":70.25,"recordedAt":"2026-06-29T08:15:00Z","notes":"Edited note"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Edited note"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-28T08:30:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-06-29T08:15:00Z"));

        verify(service).update(
                7L,
                9L,
                new BigDecimal("70.25"),
                Instant.parse("2026-06-29T08:15:00Z"),
                "Edited note"
        );
    }
}
