CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    grams_per_unit NUMERIC(10,2) NOT NULL DEFAULT 100.00,
    nutrition_basis VARCHAR(30) NOT NULL DEFAULT 'PER_100_GRAMS',
    default_price NUMERIC(10,2),
    media_id BIGINT
);

CREATE TABLE IF NOT EXISTS media (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes INTEGER NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    data BYTEA NOT NULL
);

CREATE TABLE IF NOT EXISTS nutritional_values (
    product_id BIGINT PRIMARY KEY,
    calories NUMERIC(10,2) NOT NULL,
    carbohydrates NUMERIC(10,2) NOT NULL,
    proteins NUMERIC(10,2) NOT NULL,
    fats NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_nutritional_values_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS supermarkets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_supermarkets_name_lower
    ON supermarkets (LOWER(name));

CREATE TABLE IF NOT EXISTS product_supermarkets (
    product_id BIGINT NOT NULL,
    supermarket_id BIGINT NOT NULL,
    CONSTRAINT pk_product_supermarkets PRIMARY KEY (product_id, supermarket_id),
    CONSTRAINT fk_product_supermarkets_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_product_supermarkets_supermarket
        FOREIGN KEY (supermarket_id)
        REFERENCES supermarkets(id)
);

CREATE INDEX IF NOT EXISTS idx_product_supermarkets_supermarket_id
    ON product_supermarkets(supermarket_id);

CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    instructions TEXT NOT NULL,
    default_units_produced NUMERIC(10,2),
    media_id BIGINT
);

CREATE TABLE IF NOT EXISTS recipe_products (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL,
    quantity_type VARCHAR(10) NOT NULL,
    CONSTRAINT fk_recipe_products_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_products_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT uq_recipe_products_assignment UNIQUE (recipe_id, product_id)
);

CREATE TABLE IF NOT EXISTS recipe_product_origins (
    recipe_id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    derived_product_name VARCHAR(150) NOT NULL,
    units_produced NUMERIC(10,2) NOT NULL,
    stock_from_composition BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_recipe_product_origins_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_product_origins_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipe_derived_product_ingredients (
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL,
    quantity_type VARCHAR(10) NOT NULL,
    CONSTRAINT pk_recipe_derived_product_ingredients PRIMARY KEY (recipe_id, product_id),
    CONSTRAINT fk_recipe_derived_product_ingredients_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_derived_product_ingredients_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stock_entries (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL CHECK (quantity >= 0),
    price NUMERIC(10,2) NOT NULL,
    expiration_date DATE,
    entry_date DATE NOT NULL,
    CONSTRAINT fk_stock_entries_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stock_entries_product_id ON stock_entries(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_entries_expiration_date ON stock_entries(expiration_date);

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    stock_entry_id BIGINT,
    movement_type VARCHAR(30) NOT NULL,
    signed_quantity NUMERIC(12,2) NOT NULL CHECK (signed_quantity <> 0),
    effective_date DATE NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    product_name VARCHAR(150) NOT NULL,
    price NUMERIC(12,2),
    expiration_date DATE,
    entry_date DATE,
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_effective_date ON stock_movements(effective_date DESC, recorded_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS proposed_week_menus (
    id BIGSERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    users INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_proposed_week_menus_dates CHECK (start_date <= end_date)
);

CREATE TABLE IF NOT EXISTS proposed_week_menu_days (
    id BIGSERIAL PRIMARY KEY,
    menu_id BIGINT NOT NULL,
    menu_date DATE NOT NULL,
    CONSTRAINT fk_proposed_week_menu_days_menu
        FOREIGN KEY (menu_id)
        REFERENCES proposed_week_menus(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_proposed_week_menu_days_date UNIQUE (menu_id, menu_date)
);

CREATE TABLE IF NOT EXISTS proposed_week_menu_day_parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    sort_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS proposed_week_menu_sections (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL,
    day_part_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_proposed_week_menu_sections_day
        FOREIGN KEY (day_id)
        REFERENCES proposed_week_menu_days(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_proposed_week_menu_sections_day_part
        FOREIGN KEY (day_part_id)
        REFERENCES proposed_week_menu_day_parts(id),
    CONSTRAINT uq_proposed_week_menu_sections_day_part UNIQUE (day_id, day_part_id)
);

CREATE TABLE IF NOT EXISTS proposed_week_menu_products (
    id BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(150) NOT NULL,
    units NUMERIC(10,2),
    grams NUMERIC(10,2),
    calories NUMERIC(10,2),
    carbohydrates NUMERIC(10,2),
    proteins NUMERIC(10,2),
    fats NUMERIC(10,2),
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_proposed_week_menu_products_section
        FOREIGN KEY (section_id)
        REFERENCES proposed_week_menu_sections(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_proposed_week_menu_products_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT chk_proposed_week_menu_products_manual_quantity
        CHECK (product_id IS NOT NULL OR (units IS NULL AND grams IS NULL)),
    CONSTRAINT uq_proposed_week_menu_products_order UNIQUE (section_id, sort_order)
);

CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_days_menu_id ON proposed_week_menu_days(menu_id);
CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_day_parts_sort_order ON proposed_week_menu_day_parts(sort_order);
CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_sections_day_id ON proposed_week_menu_sections(day_id);
CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_products_section_id ON proposed_week_menu_products(section_id);

CREATE TABLE IF NOT EXISTS current_week_menus (
    id BIGSERIAL PRIMARY KEY,
    proposed_week_menu_id BIGINT NOT NULL UNIQUE,
    snapshot_json TEXT NOT NULL,
    CONSTRAINT fk_current_week_menus_proposed_menu
        FOREIGN KEY (proposed_week_menu_id)
        REFERENCES proposed_week_menus(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS current_week_menu_stats (
    current_week_menu_id BIGINT PRIMARY KEY,
    stats_json TEXT NOT NULL,
    closed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_current_week_menu_stats_menu
        FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS money_boxes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    user_id BIGINT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_money_boxes_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_manual_money_boxes_name_lower
    ON money_boxes (LOWER(name))
    WHERE user_id IS NULL;

CREATE OR REPLACE FUNCTION create_user_money_box()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO money_boxes (name, user_id)
    VALUES (NEW.username, NEW.id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_create_user_money_box ON app_users;

CREATE TRIGGER trg_create_user_money_box
AFTER INSERT ON app_users
FOR EACH ROW
EXECUTE FUNCTION create_user_money_box();

CREATE TABLE IF NOT EXISTS user_menu_history (
    id BIGSERIAL PRIMARY KEY,
    current_week_menu_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    person_name VARCHAR(80) NOT NULL,
    menu_start_date DATE NOT NULL,
    menu_end_date DATE NOT NULL,
    menu_snapshot_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_menu_history_menu FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_menu_history_person FOREIGN KEY (person_id)
        REFERENCES app_users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_user_menu_history_menu_person UNIQUE (current_week_menu_id, person_id)
);

CREATE INDEX IF NOT EXISTS idx_user_menu_history_person_end_date
    ON user_menu_history(person_id, menu_end_date);

CREATE TABLE IF NOT EXISTS user_money_movements (
    id BIGSERIAL PRIMARY KEY,
    money_box_id BIGINT NOT NULL,
    user_id BIGINT,
    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(255),
    current_week_menu_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_money_movements_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_money_movements_money_box
        FOREIGN KEY (money_box_id)
        REFERENCES money_boxes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_money_movements_current_week_menu
        FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_user_money_movements_user_created
    ON user_money_movements(user_id, created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_user_money_movements_box_created
    ON user_money_movements(money_box_id, created_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS planning_coupon_redemptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_code VARCHAR(80) NOT NULL,
    planning_id BIGINT NOT NULL,
    current_week_menu_id BIGINT NOT NULL,
    reward_amount NUMERIC(10,2) NOT NULL,
    used_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_planning_coupon_redemptions_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_planning_coupon_redemptions_planning
        FOREIGN KEY (planning_id)
        REFERENCES proposed_week_menus(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_planning_coupon_redemptions_menu
        FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_planning_coupon_redemptions_menu_coupon UNIQUE (current_week_menu_id, coupon_code)
);

CREATE INDEX IF NOT EXISTS idx_planning_coupon_redemptions_user_coupon_used
    ON planning_coupon_redemptions(user_id, coupon_code, used_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS user_weight_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    weight NUMERIC(6, 2) NOT NULL CHECK (weight > 0),
    recorded_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    notes TEXT,
    CONSTRAINT fk_user_weight_entries_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_weight_entries_user_recorded
    ON user_weight_entries(user_id, recorded_at, id);

CREATE TABLE IF NOT EXISTS menu_stock_movements (
    id BIGSERIAL PRIMARY KEY,
    current_week_menu_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_username VARCHAR(80) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL CHECK (quantity > 0),
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    total_cost NUMERIC(12,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_menu_stock_movements_menu
        FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_menu_stock_movements_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_menu_stock_movements_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS proposed_week_menu_recipe_productions (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    units NUMERIC(12,2) NOT NULL,
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

CREATE TABLE IF NOT EXISTS nutritional_rules (
    id SMALLINT PRIMARY KEY,
    daily_calories_minimum NUMERIC(10,2),
    daily_calories_maximum NUMERIC(10,2),
    daily_carbohydrates_minimum NUMERIC(10,2),
    daily_carbohydrates_maximum NUMERIC(10,2),
    daily_proteins_minimum NUMERIC(10,2),
    daily_proteins_maximum NUMERIC(10,2),
    daily_fats_minimum NUMERIC(10,2),
    daily_fats_maximum NUMERIC(10,2),
    weekly_calories_minimum NUMERIC(10,2),
    weekly_calories_maximum NUMERIC(10,2),
    weekly_carbohydrates_minimum NUMERIC(10,2),
    weekly_carbohydrates_maximum NUMERIC(10,2),
    weekly_proteins_minimum NUMERIC(10,2),
    weekly_proteins_maximum NUMERIC(10,2),
    weekly_fats_minimum NUMERIC(10,2),
    weekly_fats_maximum NUMERIC(10,2),
    CONSTRAINT chk_nutritional_rules_singleton CHECK (id = 1),
    CONSTRAINT chk_nutritional_rules_daily_non_negative CHECK (
        (daily_calories_minimum IS NULL OR daily_calories_minimum >= 0) AND
        (daily_calories_maximum IS NULL OR daily_calories_maximum >= 0) AND
        (daily_carbohydrates_minimum IS NULL OR daily_carbohydrates_minimum >= 0) AND
        (daily_carbohydrates_maximum IS NULL OR daily_carbohydrates_maximum >= 0) AND
        (daily_proteins_minimum IS NULL OR daily_proteins_minimum >= 0) AND
        (daily_proteins_maximum IS NULL OR daily_proteins_maximum >= 0) AND
        (daily_fats_minimum IS NULL OR daily_fats_minimum >= 0) AND
        (daily_fats_maximum IS NULL OR daily_fats_maximum >= 0)
    ),
    CONSTRAINT chk_nutritional_rules_daily_calories CHECK (daily_calories_minimum IS NULL OR daily_calories_maximum IS NULL OR daily_calories_minimum <= daily_calories_maximum),
    CONSTRAINT chk_nutritional_rules_daily_carbohydrates CHECK (daily_carbohydrates_minimum IS NULL OR daily_carbohydrates_maximum IS NULL OR daily_carbohydrates_minimum <= daily_carbohydrates_maximum),
    CONSTRAINT chk_nutritional_rules_daily_proteins CHECK (daily_proteins_minimum IS NULL OR daily_proteins_maximum IS NULL OR daily_proteins_minimum <= daily_proteins_maximum),
    CONSTRAINT chk_nutritional_rules_daily_fats CHECK (daily_fats_minimum IS NULL OR daily_fats_maximum IS NULL OR daily_fats_minimum <= daily_fats_maximum),
    CONSTRAINT chk_nutritional_rules_weekly_non_negative CHECK (
        (weekly_calories_minimum IS NULL OR weekly_calories_minimum >= 0) AND
        (weekly_calories_maximum IS NULL OR weekly_calories_maximum >= 0) AND
        (weekly_carbohydrates_minimum IS NULL OR weekly_carbohydrates_minimum >= 0) AND
        (weekly_carbohydrates_maximum IS NULL OR weekly_carbohydrates_maximum >= 0) AND
        (weekly_proteins_minimum IS NULL OR weekly_proteins_minimum >= 0) AND
        (weekly_proteins_maximum IS NULL OR weekly_proteins_maximum >= 0) AND
        (weekly_fats_minimum IS NULL OR weekly_fats_minimum >= 0) AND
        (weekly_fats_maximum IS NULL OR weekly_fats_maximum >= 0)
    ),
    CONSTRAINT chk_nutritional_rules_weekly_calories CHECK (weekly_calories_minimum IS NULL OR weekly_calories_maximum IS NULL OR weekly_calories_minimum <= weekly_calories_maximum),
    CONSTRAINT chk_nutritional_rules_weekly_carbohydrates CHECK (weekly_carbohydrates_minimum IS NULL OR weekly_carbohydrates_maximum IS NULL OR weekly_carbohydrates_minimum <= weekly_carbohydrates_maximum),
    CONSTRAINT chk_nutritional_rules_weekly_proteins CHECK (weekly_proteins_minimum IS NULL OR weekly_proteins_maximum IS NULL OR weekly_proteins_minimum <= weekly_proteins_maximum),
    CONSTRAINT chk_nutritional_rules_weekly_fats CHECK (weekly_fats_minimum IS NULL OR weekly_fats_maximum IS NULL OR weekly_fats_minimum <= weekly_fats_maximum)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_products_media'
          AND table_name = 'products'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_media
                FOREIGN KEY (media_id)
                REFERENCES media(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_recipes_media'
          AND table_name = 'recipes'
    ) THEN
        ALTER TABLE recipes
            ADD CONSTRAINT fk_recipes_media
                FOREIGN KEY (media_id)
                REFERENCES media(id);
    END IF;
END $$;
