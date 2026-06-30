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

CREATE INDEX idx_menu_stock_movements_menu_id
    ON menu_stock_movements(current_week_menu_id, created_at, id);
