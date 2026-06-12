ALTER TABLE products
    ADD COLUMN IF NOT EXISTS grams_per_unit NUMERIC(10,2) NOT NULL DEFAULT 100.00;

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

CREATE TABLE IF NOT EXISTS proposed_week_menu_sections (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_proposed_week_menu_sections_day
        FOREIGN KEY (day_id)
        REFERENCES proposed_week_menu_days(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_proposed_week_menu_sections_order UNIQUE (day_id, sort_order)
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
CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_sections_day_id ON proposed_week_menu_sections(day_id);
CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_products_section_id ON proposed_week_menu_products(section_id);
