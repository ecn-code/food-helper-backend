package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class MenuResponseCompatibilityTest {
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void shouldReadLegacySnapshotIdentifiersAndPeriodName() {
        CurrentWeekMenuResponse menu = objectMapper.readValue(
                "{\"id\":1,\"proposedWeekMenuId\":5}",
                CurrentWeekMenuResponse.class
        );
        CurrentWeekMenuStatsResponse stats = objectMapper.readValue(
                "{\"currentWeekMenuId\":1,\"week\":{\"averageCalories\":100}}",
                CurrentWeekMenuStatsResponse.class
        );

        assertThat(menu.planningId()).isEqualTo(5L);
        assertThat(stats.menuId()).isEqualTo(1L);
        assertThat(stats.period().averageCalories()).isEqualByComparingTo("100");
    }
}
