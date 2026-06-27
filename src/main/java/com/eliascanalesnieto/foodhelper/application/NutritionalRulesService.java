package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.NutritionalRules;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRulesRepository;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleEvaluationResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleRequest;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRuleStatus;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesEvaluationResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalValuesResponse;
import com.eliascanalesnieto.foodhelper.presentation.SaveNutritionalRulesRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NutritionalRulesService {
    private static final int SCALE = 2;

    private final NutritionalRulesRepository repository;

    @Transactional(readOnly = true)
    public NutritionalRulesResponse find() {
        return toResponse(repository.find());
    }

    @Transactional
    public NutritionalRulesResponse save(SaveNutritionalRulesRequest request) {
        validate("calories", request.calories());
        validate("carbohydrates", request.carbohydrates());
        validate("proteins", request.proteins());
        validate("fats", request.fats());
        return toResponse(repository.save(NutritionalRules.builder()
                .caloriesMinimum(request.calories().minimum())
                .caloriesMaximum(request.calories().maximum())
                .carbohydratesMinimum(request.carbohydrates().minimum())
                .carbohydratesMaximum(request.carbohydrates().maximum())
                .proteinsMinimum(request.proteins().minimum())
                .proteinsMaximum(request.proteins().maximum())
                .fatsMinimum(request.fats().minimum())
                .fatsMaximum(request.fats().maximum())
                .build()));
    }

    @Transactional(readOnly = true)
    public NutritionalRulesEvaluationResponse evaluate(NutritionalValues totals, int plannedDays) {
        return evaluate(totals.getCalories(), totals.getCarbohydrates(), totals.getProteins(), totals.getFats(), plannedDays);
    }

    @Transactional(readOnly = true)
    public NutritionalRulesEvaluationResponse evaluate(NutritionalValuesResponse totals, int plannedDays) {
        return evaluate(totals.calories(), totals.carbohydrates(), totals.proteins(), totals.fats(), plannedDays);
    }

    private NutritionalRulesEvaluationResponse evaluate(
            BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, int plannedDays
    ) {
        NutritionalRules rules = repository.find();
        int divisor = Math.max(plannedDays, 1);
        return new NutritionalRulesEvaluationResponse(
                plannedDays,
                evaluate(calories, divisor, rules.getCaloriesMinimum(), rules.getCaloriesMaximum()),
                evaluate(carbohydrates, divisor, rules.getCarbohydratesMinimum(), rules.getCarbohydratesMaximum()),
                evaluate(proteins, divisor, rules.getProteinsMinimum(), rules.getProteinsMaximum()),
                evaluate(fats, divisor, rules.getFatsMinimum(), rules.getFatsMaximum())
        );
    }

    private void validate(String nutrient, NutrientRuleRequest rule) {
        if (rule.minimum() != null && rule.maximum() != null && rule.minimum().compareTo(rule.maximum()) > 0) {
            throw new IllegalArgumentException(nutrient + " minimum cannot be greater than maximum");
        }
    }

    private NutrientRuleEvaluationResponse evaluate(BigDecimal total, int divisor, BigDecimal minimum, BigDecimal maximum) {
        BigDecimal value = total.divide(BigDecimal.valueOf(divisor), SCALE, RoundingMode.HALF_UP);
        NutritionalRuleStatus status;
        if (minimum == null && maximum == null) {
            status = NutritionalRuleStatus.NOT_CONFIGURED;
        } else if (minimum != null && value.compareTo(minimum) < 0) {
            status = NutritionalRuleStatus.BELOW_MINIMUM;
        } else if (maximum != null && value.compareTo(maximum) > 0) {
            status = NutritionalRuleStatus.ABOVE_MAXIMUM;
        } else {
            status = NutritionalRuleStatus.WITHIN_RANGE;
        }
        return new NutrientRuleEvaluationResponse(value, minimum, maximum, status);
    }

    private NutritionalRulesResponse toResponse(NutritionalRules rules) {
        return new NutritionalRulesResponse(
                new NutrientRuleResponse(rules.getCaloriesMinimum(), rules.getCaloriesMaximum()),
                new NutrientRuleResponse(rules.getCarbohydratesMinimum(), rules.getCarbohydratesMaximum()),
                new NutrientRuleResponse(rules.getProteinsMinimum(), rules.getProteinsMaximum()),
                new NutrientRuleResponse(rules.getFatsMinimum(), rules.getFatsMaximum())
        );
    }
}
