package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.NutritionalRules;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRulesRepository;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRuleSet;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcNutritionalRulesRepository implements NutritionalRulesRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public NutritionalRules find() {
        return jdbcTemplate.query("SELECT * FROM nutritional_rules WHERE id = 1", (rs, rowNum) -> NutritionalRules.builder()
                        .daily(toRuleSet(rs, "daily_"))
                        .weekly(toRuleSet(rs, "weekly_"))
                        .build())
                .stream()
                .findFirst()
                .orElseGet(() -> NutritionalRules.builder()
                        .daily(NutritionalRuleSet.builder().build())
                        .weekly(NutritionalRuleSet.builder().build())
                        .build());
    }

    @Override
    public NutritionalRules save(NutritionalRules rules) {
        jdbcTemplate.update("""
                INSERT INTO nutritional_rules (
                    id,
                    daily_calories_minimum, daily_calories_maximum,
                    daily_carbohydrates_minimum, daily_carbohydrates_maximum,
                    daily_proteins_minimum, daily_proteins_maximum,
                    daily_fats_minimum, daily_fats_maximum,
                    weekly_calories_minimum, weekly_calories_maximum,
                    weekly_carbohydrates_minimum, weekly_carbohydrates_maximum,
                    weekly_proteins_minimum, weekly_proteins_maximum,
                    weekly_fats_minimum, weekly_fats_maximum
                ) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    daily_calories_minimum = EXCLUDED.daily_calories_minimum,
                    daily_calories_maximum = EXCLUDED.daily_calories_maximum,
                    daily_carbohydrates_minimum = EXCLUDED.daily_carbohydrates_minimum,
                    daily_carbohydrates_maximum = EXCLUDED.daily_carbohydrates_maximum,
                    daily_proteins_minimum = EXCLUDED.daily_proteins_minimum,
                    daily_proteins_maximum = EXCLUDED.daily_proteins_maximum,
                    daily_fats_minimum = EXCLUDED.daily_fats_minimum,
                    daily_fats_maximum = EXCLUDED.daily_fats_maximum,
                    weekly_calories_minimum = EXCLUDED.weekly_calories_minimum,
                    weekly_calories_maximum = EXCLUDED.weekly_calories_maximum,
                    weekly_carbohydrates_minimum = EXCLUDED.weekly_carbohydrates_minimum,
                    weekly_carbohydrates_maximum = EXCLUDED.weekly_carbohydrates_maximum,
                    weekly_proteins_minimum = EXCLUDED.weekly_proteins_minimum,
                    weekly_proteins_maximum = EXCLUDED.weekly_proteins_maximum,
                    weekly_fats_minimum = EXCLUDED.weekly_fats_minimum,
                    weekly_fats_maximum = EXCLUDED.weekly_fats_maximum
                """,
                rules.getDaily().getCaloriesMinimum(), rules.getDaily().getCaloriesMaximum(),
                rules.getDaily().getCarbohydratesMinimum(), rules.getDaily().getCarbohydratesMaximum(),
                rules.getDaily().getProteinsMinimum(), rules.getDaily().getProteinsMaximum(),
                rules.getDaily().getFatsMinimum(), rules.getDaily().getFatsMaximum(),
                rules.getWeekly().getCaloriesMinimum(), rules.getWeekly().getCaloriesMaximum(),
                rules.getWeekly().getCarbohydratesMinimum(), rules.getWeekly().getCarbohydratesMaximum(),
                rules.getWeekly().getProteinsMinimum(), rules.getWeekly().getProteinsMaximum(),
                rules.getWeekly().getFatsMinimum(), rules.getWeekly().getFatsMaximum());
        return find();
    }

    private NutritionalRuleSet toRuleSet(java.sql.ResultSet rs, String prefix) throws java.sql.SQLException {
        return NutritionalRuleSet.builder()
                .caloriesMinimum(rs.getBigDecimal(prefix + "calories_minimum"))
                .caloriesMaximum(rs.getBigDecimal(prefix + "calories_maximum"))
                .carbohydratesMinimum(rs.getBigDecimal(prefix + "carbohydrates_minimum"))
                .carbohydratesMaximum(rs.getBigDecimal(prefix + "carbohydrates_maximum"))
                .proteinsMinimum(rs.getBigDecimal(prefix + "proteins_minimum"))
                .proteinsMaximum(rs.getBigDecimal(prefix + "proteins_maximum"))
                .fatsMinimum(rs.getBigDecimal(prefix + "fats_minimum"))
                .fatsMaximum(rs.getBigDecimal(prefix + "fats_maximum"))
                .build();
    }
}
