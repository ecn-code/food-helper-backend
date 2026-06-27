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
