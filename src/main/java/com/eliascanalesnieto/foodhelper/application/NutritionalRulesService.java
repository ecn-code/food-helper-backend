package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.NutritionalRules;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRulesRepository;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRuleSet;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleEvaluationResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleRequest;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRuleStatus;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesEvaluationResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesEvaluationPeriodResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesPeriodRequest;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesPeriodResponse;
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
        validate("calories", request.daily().calories());
        validate("carbohydrates", request.daily().carbohydrates());
        validate("proteins", request.daily().proteins());
        validate("fats", request.daily().fats());
        validate("calories", request.weekly().calories());
        validate("carbohydrates", request.weekly().carbohydrates());
        validate("proteins", request.weekly().proteins());
        validate("fats", request.weekly().fats());
        return toResponse(repository.save(NutritionalRules.builder()
                .daily(toDomain(request.daily()))
                .weekly(toDomain(request.weekly()))
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

    /** Evaluates one planned day against the configured daily limits. */
    @Transactional(readOnly = true)
    public NutritionalRulesEvaluationPeriodResponse evaluateDaily(NutritionalValuesResponse totals) {
        NutritionalValuesResponse safeTotals = totals == null
                ? new NutritionalValuesResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
                : totals;
        NutritionalRules rules = repository.find();
        return evaluatePeriod(
                safeTotals.calories(), safeTotals.carbohydrates(), safeTotals.proteins(), safeTotals.fats(),
                1, 1, rules.getDaily());
    }

    private NutritionalRulesEvaluationResponse evaluate(
            BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, int plannedDays
    ) {
        NutritionalRules rules = repository.find();
        int divisor = Math.max(plannedDays, 1);
        return new NutritionalRulesEvaluationResponse(
                evaluatePeriod(calories, carbohydrates, proteins, fats, plannedDays, divisor, rules.getDaily()),
                evaluatePeriod(calories, carbohydrates, proteins, fats, plannedDays, divisor, rules.getWeekly())
        );
    }

    private NutritionalRulesEvaluationPeriodResponse evaluatePeriod(
            BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats,
            int plannedDays, int divisor, NutritionalRuleSet ruleSet
    ) {
        NutritionalRuleSet effectiveRuleSet = ruleSet == null ? NutritionalRuleSet.builder().build() : ruleSet;
        return new NutritionalRulesEvaluationPeriodResponse(
                plannedDays,
                evaluate(calories, divisor, effectiveRuleSet.getCaloriesMinimum(), effectiveRuleSet.getCaloriesMaximum()),
                evaluate(carbohydrates, divisor, effectiveRuleSet.getCarbohydratesMinimum(), effectiveRuleSet.getCarbohydratesMaximum()),
                evaluate(proteins, divisor, effectiveRuleSet.getProteinsMinimum(), effectiveRuleSet.getProteinsMaximum()),
                evaluate(fats, divisor, effectiveRuleSet.getFatsMinimum(), effectiveRuleSet.getFatsMaximum())
        );
    }

    private void validate(String nutrient, NutrientRuleRequest rule) {
        if (rule.minimum() != null && rule.maximum() != null && rule.minimum().compareTo(rule.maximum()) > 0) {
            throw new IllegalArgumentException(nutrient + " minimum cannot be greater than maximum");
        }
    }

    private NutritionalRuleSet toDomain(NutritionalRulesPeriodRequest request) {
        return NutritionalRuleSet.builder()
                .caloriesMinimum(request.calories().minimum())
                .caloriesMaximum(request.calories().maximum())
                .carbohydratesMinimum(request.carbohydrates().minimum())
                .carbohydratesMaximum(request.carbohydrates().maximum())
                .proteinsMinimum(request.proteins().minimum())
                .proteinsMaximum(request.proteins().maximum())
                .fatsMinimum(request.fats().minimum())
                .fatsMaximum(request.fats().maximum())
                .build();
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
        NutritionalRulesPeriodResponse daily = toResponse(rules.getDaily());
        NutritionalRulesPeriodResponse weekly = toResponse(rules.getWeekly());
        return new NutritionalRulesResponse(daily, weekly);
    }

    private NutritionalRulesPeriodResponse toResponse(NutritionalRuleSet ruleSet) {
        NutritionalRuleSet effectiveRuleSet = ruleSet == null ? NutritionalRuleSet.builder().build() : ruleSet;
        return new NutritionalRulesPeriodResponse(
                new NutrientRuleResponse(effectiveRuleSet.getCaloriesMinimum(), effectiveRuleSet.getCaloriesMaximum()),
                new NutrientRuleResponse(effectiveRuleSet.getCarbohydratesMinimum(), effectiveRuleSet.getCarbohydratesMaximum()),
                new NutrientRuleResponse(effectiveRuleSet.getProteinsMinimum(), effectiveRuleSet.getProteinsMaximum()),
                new NutrientRuleResponse(effectiveRuleSet.getFatsMinimum(), effectiveRuleSet.getFatsMaximum())
        );
    }
}
