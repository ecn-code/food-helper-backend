package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.UserWeightService;
import com.eliascanalesnieto.foodhelper.domain.UserWeightEntry;
import com.eliascanalesnieto.foodhelper.domain.UserWeightRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserWeightServiceTest {
    @Mock
    private UserWeightRepository repository;

    @InjectMocks
    private UserWeightService service;

    @Test
    void statsShouldReturnOldestHighestAndLowestMeasurements() {
        Long userId = 7L;
        Instant from = Instant.parse("2026-06-01T00:00:00Z");
        Instant firstHighestAt = Instant.parse("2026-06-10T07:30:00Z");
        Instant lowestAt = Instant.parse("2026-06-15T18:45:00Z");
        Instant tiedHighestAt = Instant.parse("2026-06-20T09:15:00Z");
        Instant to = Instant.parse("2026-06-30T23:59:59Z");
        when(repository.findByPeriod(userId, from, to)).thenReturn(List.of(
                entry(1L, userId, "75.40", firstHighestAt),
                entry(2L, userId, "72.10", lowestAt),
                entry(3L, userId, "75.40", tiedHighestAt)
        ));

        var stats = service.findStats(userId, from, to);

        assertThat(stats.highest().weight()).isEqualByComparingTo("75.40");
        assertThat(stats.highest().recordedAt()).isEqualTo(firstHighestAt);
        assertThat(stats.lowest().weight()).isEqualByComparingTo("72.10");
        assertThat(stats.lowest().recordedAt()).isEqualTo(lowestAt);
    }

    @Test
    void statsShouldReturnNullExtremesForAnEmptyPeriod() {
        Long userId = 7L;
        Instant from = Instant.parse("2026-06-01T00:00:00Z");
        Instant to = Instant.parse("2026-06-30T23:59:59Z");
        when(repository.findByPeriod(userId, from, to)).thenReturn(List.of());

        var stats = service.findStats(userId, from, to);

        assertThat(stats.highest()).isNull();
        assertThat(stats.lowest()).isNull();
    }

    @Test
    void periodStartAfterEndShouldBeRejected() {
        assertThatThrownBy(() -> service.findByPeriod(
                7L,
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Period start must not be after period end");
    }

    private UserWeightEntry entry(Long id, Long userId, String weight, Instant recordedAt) {
        return UserWeightEntry.builder()
                .id(id)
                .userId(userId)
                .weight(new BigDecimal(weight))
                .recordedAt(recordedAt)
                .build();
    }
}
