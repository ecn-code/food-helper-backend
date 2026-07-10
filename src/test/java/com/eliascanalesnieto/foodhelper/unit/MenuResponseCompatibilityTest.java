package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState;
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
        assertThat(menu.personIds()).isEmpty();
        assertThat(menu.state()).isEqualTo(CurrentWeekMenuState.ESTABLISHED);
        assertThat(menu.isActive()).isTrue();
        assertThat(menu.canEdit()).isTrue();
        assertThat(menu.canDelete()).isTrue();
        assertThat(menu.canClose()).isTrue();
        assertThat(menu.canUndo()).isTrue();
        assertThat(stats.menuId()).isEqualTo(1L);
        assertThat(stats.period().averageCalories()).isEqualByComparingTo("100");
    }
}
