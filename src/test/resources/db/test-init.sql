CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    grams_per_unit NUMERIC(10,2) NOT NULL DEFAULT 100.00,
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
    media_id BIGINT
);

CREATE TABLE IF NOT EXISTS recipe_products (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    grams NUMERIC(10,2) NOT NULL,
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
    produced_grams NUMERIC(10,2) NOT NULL,
    grams_per_unit NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_recipe_product_origins_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_product_origins_product
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

CREATE TABLE IF NOT EXISTS proposed_week_menus (
    id BIGSERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
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
    product_id BIGINT NOT NULL,
    units NUMERIC(10,2) NOT NULL,
    grams NUMERIC(10,2) NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_proposed_week_menu_products_section
        FOREIGN KEY (section_id)
        REFERENCES proposed_week_menu_sections(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_proposed_week_menu_products_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),
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

CREATE TABLE IF NOT EXISTS user_money_movements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    description VARCHAR(255),
    current_week_menu_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_money_movements_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_money_movements_current_week_menu
        FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_user_money_movements_user_created
    ON user_money_movements(user_id, created_at DESC, id DESC);

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
