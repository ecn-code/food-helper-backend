CREATE TABLE IF NOT EXISTS nutritional_rules (
    id SMALLINT PRIMARY KEY,
    calories_minimum NUMERIC(10,2),
    calories_maximum NUMERIC(10,2),
    carbohydrates_minimum NUMERIC(10,2),
    carbohydrates_maximum NUMERIC(10,2),
    proteins_minimum NUMERIC(10,2),
    proteins_maximum NUMERIC(10,2),
    fats_minimum NUMERIC(10,2),
    fats_maximum NUMERIC(10,2),
    CONSTRAINT chk_nutritional_rules_singleton CHECK (id = 1),
    CONSTRAINT chk_nutritional_rules_non_negative CHECK (
        (calories_minimum IS NULL OR calories_minimum >= 0) AND
        (calories_maximum IS NULL OR calories_maximum >= 0) AND
        (carbohydrates_minimum IS NULL OR carbohydrates_minimum >= 0) AND
        (carbohydrates_maximum IS NULL OR carbohydrates_maximum >= 0) AND
        (proteins_minimum IS NULL OR proteins_minimum >= 0) AND
        (proteins_maximum IS NULL OR proteins_maximum >= 0) AND
        (fats_minimum IS NULL OR fats_minimum >= 0) AND
        (fats_maximum IS NULL OR fats_maximum >= 0)
    ),
    CONSTRAINT chk_nutritional_rules_calories CHECK (calories_minimum IS NULL OR calories_maximum IS NULL OR calories_minimum <= calories_maximum),
    CONSTRAINT chk_nutritional_rules_carbohydrates CHECK (carbohydrates_minimum IS NULL OR carbohydrates_maximum IS NULL OR carbohydrates_minimum <= carbohydrates_maximum),
    CONSTRAINT chk_nutritional_rules_proteins CHECK (proteins_minimum IS NULL OR proteins_maximum IS NULL OR proteins_minimum <= proteins_maximum),
    CONSTRAINT chk_nutritional_rules_fats CHECK (fats_minimum IS NULL OR fats_maximum IS NULL OR fats_minimum <= fats_maximum)
);
