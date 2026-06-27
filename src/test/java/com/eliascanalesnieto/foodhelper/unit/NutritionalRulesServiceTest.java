package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRules;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRulesRepository;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleRequest;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRuleStatus;
import com.eliascanalesnieto.foodhelper.presentation.SaveNutritionalRulesRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionalRulesServiceTest {
    @Mock
    private NutritionalRulesRepository repository;

    @InjectMocks
    private NutritionalRulesService service;

    @Test
    void shouldEvaluateAveragePerPlannedDay() {
        when(repository.find()).thenReturn(NutritionalRules.builder()
                .caloriesMinimum(new BigDecimal("100"))
                .caloriesMaximum(new BigDecimal("200"))
                .carbohydratesMaximum(new BigDecimal("20"))
                .proteinsMinimum(new BigDecimal("10"))
                .build());

        var result = service.evaluate(NutritionalValues.builder()
                .calories(new BigDecimal("300"))
                .carbohydrates(new BigDecimal("50"))
                .proteins(new BigDecimal("10"))
                .fats(new BigDecimal("4"))
                .build(), 2);

        assertThat(result.plannedDays()).isEqualTo(2);
        assertThat(result.calories().value()).isEqualByComparingTo("150.00");
        assertThat(result.calories().status()).isEqualTo(NutritionalRuleStatus.WITHIN_RANGE);
        assertThat(result.carbohydrates().status()).isEqualTo(NutritionalRuleStatus.ABOVE_MAXIMUM);
        assertThat(result.proteins().status()).isEqualTo(NutritionalRuleStatus.BELOW_MINIMUM);
        assertThat(result.fats().status()).isEqualTo(NutritionalRuleStatus.NOT_CONFIGURED);
    }

    @Test
    void shouldRejectMinimumGreaterThanMaximum() {
        SaveNutritionalRulesRequest request = new SaveNutritionalRulesRequest(
                new NutrientRuleRequest(new BigDecimal("200"), new BigDecimal("100")),
                new NutrientRuleRequest(null, null),
                new NutrientRuleRequest(null, null),
                new NutrientRuleRequest(null, null)
        );

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("calories minimum cannot be greater than maximum");
    }
}
