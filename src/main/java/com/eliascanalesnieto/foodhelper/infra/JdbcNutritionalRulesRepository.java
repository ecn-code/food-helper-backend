package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.NutritionalRules;
import com.eliascanalesnieto.foodhelper.domain.NutritionalRulesRepository;
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
                        .caloriesMinimum(rs.getBigDecimal("calories_minimum"))
                        .caloriesMaximum(rs.getBigDecimal("calories_maximum"))
                        .carbohydratesMinimum(rs.getBigDecimal("carbohydrates_minimum"))
                        .carbohydratesMaximum(rs.getBigDecimal("carbohydrates_maximum"))
                        .proteinsMinimum(rs.getBigDecimal("proteins_minimum"))
                        .proteinsMaximum(rs.getBigDecimal("proteins_maximum"))
                        .fatsMinimum(rs.getBigDecimal("fats_minimum"))
                        .fatsMaximum(rs.getBigDecimal("fats_maximum"))
                        .build())
                .stream()
                .findFirst()
                .orElseGet(() -> NutritionalRules.builder().build());
    }

    @Override
    public NutritionalRules save(NutritionalRules rules) {
        jdbcTemplate.update("""
                INSERT INTO nutritional_rules (
                    id, calories_minimum, calories_maximum, carbohydrates_minimum, carbohydrates_maximum,
                    proteins_minimum, proteins_maximum, fats_minimum, fats_maximum
                ) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    calories_minimum = EXCLUDED.calories_minimum,
                    calories_maximum = EXCLUDED.calories_maximum,
                    carbohydrates_minimum = EXCLUDED.carbohydrates_minimum,
                    carbohydrates_maximum = EXCLUDED.carbohydrates_maximum,
                    proteins_minimum = EXCLUDED.proteins_minimum,
                    proteins_maximum = EXCLUDED.proteins_maximum,
                    fats_minimum = EXCLUDED.fats_minimum,
                    fats_maximum = EXCLUDED.fats_maximum
                """,
                rules.getCaloriesMinimum(), rules.getCaloriesMaximum(),
                rules.getCarbohydratesMinimum(), rules.getCarbohydratesMaximum(),
                rules.getProteinsMinimum(), rules.getProteinsMaximum(),
                rules.getFatsMinimum(), rules.getFatsMaximum());
        return find();
    }
}
