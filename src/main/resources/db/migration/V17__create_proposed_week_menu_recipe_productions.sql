CREATE TABLE IF NOT EXISTS proposed_week_menu_recipe_productions (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    produced_grams NUMERIC(12,2) NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT fk_proposed_week_menu_recipe_productions_day
        FOREIGN KEY (day_id)
        REFERENCES proposed_week_menu_days(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_proposed_week_menu_recipe_productions_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id),
    CONSTRAINT uq_proposed_week_menu_recipe_productions_order UNIQUE (day_id, sort_order)
);

CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_recipe_productions_day_id
    ON proposed_week_menu_recipe_productions(day_id);
