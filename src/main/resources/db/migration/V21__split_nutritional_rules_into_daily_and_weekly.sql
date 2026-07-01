ALTER TABLE nutritional_rules
    RENAME COLUMN calories_minimum TO daily_calories_minimum;

ALTER TABLE nutritional_rules
    RENAME COLUMN calories_maximum TO daily_calories_maximum;

ALTER TABLE nutritional_rules
    RENAME COLUMN carbohydrates_minimum TO daily_carbohydrates_minimum;

ALTER TABLE nutritional_rules
    RENAME COLUMN carbohydrates_maximum TO daily_carbohydrates_maximum;

ALTER TABLE nutritional_rules
    RENAME COLUMN proteins_minimum TO daily_proteins_minimum;

ALTER TABLE nutritional_rules
    RENAME COLUMN proteins_maximum TO daily_proteins_maximum;

ALTER TABLE nutritional_rules
    RENAME COLUMN fats_minimum TO daily_fats_minimum;

ALTER TABLE nutritional_rules
    RENAME COLUMN fats_maximum TO daily_fats_maximum;

ALTER TABLE nutritional_rules
    RENAME CONSTRAINT chk_nutritional_rules_calories TO chk_nutritional_rules_daily_calories;

ALTER TABLE nutritional_rules
    RENAME CONSTRAINT chk_nutritional_rules_carbohydrates TO chk_nutritional_rules_daily_carbohydrates;

ALTER TABLE nutritional_rules
    RENAME CONSTRAINT chk_nutritional_rules_proteins TO chk_nutritional_rules_daily_proteins;

ALTER TABLE nutritional_rules
    RENAME CONSTRAINT chk_nutritional_rules_fats TO chk_nutritional_rules_daily_fats;

ALTER TABLE nutritional_rules
    RENAME CONSTRAINT chk_nutritional_rules_non_negative TO chk_nutritional_rules_daily_non_negative;

ALTER TABLE nutritional_rules
    ADD COLUMN weekly_calories_minimum NUMERIC(10,2),
    ADD COLUMN weekly_calories_maximum NUMERIC(10,2),
    ADD COLUMN weekly_carbohydrates_minimum NUMERIC(10,2),
    ADD COLUMN weekly_carbohydrates_maximum NUMERIC(10,2),
    ADD COLUMN weekly_proteins_minimum NUMERIC(10,2),
    ADD COLUMN weekly_proteins_maximum NUMERIC(10,2),
    ADD COLUMN weekly_fats_minimum NUMERIC(10,2),
    ADD COLUMN weekly_fats_maximum NUMERIC(10,2);

UPDATE nutritional_rules
SET
    weekly_calories_minimum = daily_calories_minimum,
    weekly_calories_maximum = daily_calories_maximum,
    weekly_carbohydrates_minimum = daily_carbohydrates_minimum,
    weekly_carbohydrates_maximum = daily_carbohydrates_maximum,
    weekly_proteins_minimum = daily_proteins_minimum,
    weekly_proteins_maximum = daily_proteins_maximum,
    weekly_fats_minimum = daily_fats_minimum,
    weekly_fats_maximum = daily_fats_maximum
WHERE id = 1;

ALTER TABLE nutritional_rules
    ADD CONSTRAINT chk_nutritional_rules_weekly_non_negative CHECK (
        (weekly_calories_minimum IS NULL OR weekly_calories_minimum >= 0) AND
        (weekly_calories_maximum IS NULL OR weekly_calories_maximum >= 0) AND
        (weekly_carbohydrates_minimum IS NULL OR weekly_carbohydrates_minimum >= 0) AND
        (weekly_carbohydrates_maximum IS NULL OR weekly_carbohydrates_maximum >= 0) AND
        (weekly_proteins_minimum IS NULL OR weekly_proteins_minimum >= 0) AND
        (weekly_proteins_maximum IS NULL OR weekly_proteins_maximum >= 0) AND
        (weekly_fats_minimum IS NULL OR weekly_fats_minimum >= 0) AND
        (weekly_fats_maximum IS NULL OR weekly_fats_maximum >= 0)
    ),
    ADD CONSTRAINT chk_nutritional_rules_weekly_calories CHECK (
        weekly_calories_minimum IS NULL OR weekly_calories_maximum IS NULL OR weekly_calories_minimum <= weekly_calories_maximum
    ),
    ADD CONSTRAINT chk_nutritional_rules_weekly_carbohydrates CHECK (
        weekly_carbohydrates_minimum IS NULL OR weekly_carbohydrates_maximum IS NULL OR weekly_carbohydrates_minimum <= weekly_carbohydrates_maximum
    ),
    ADD CONSTRAINT chk_nutritional_rules_weekly_proteins CHECK (
        weekly_proteins_minimum IS NULL OR weekly_proteins_maximum IS NULL OR weekly_proteins_minimum <= weekly_proteins_maximum
    ),
    ADD CONSTRAINT chk_nutritional_rules_weekly_fats CHECK (
        weekly_fats_minimum IS NULL OR weekly_fats_maximum IS NULL OR weekly_fats_minimum <= weekly_fats_maximum
    );
