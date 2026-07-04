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
