CREATE TABLE IF NOT EXISTS current_week_menu_stats (
    current_week_menu_id BIGINT PRIMARY KEY,
    stats_json TEXT NOT NULL,
    closed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_current_week_menu_stats_menu
        FOREIGN KEY (current_week_menu_id)
        REFERENCES current_week_menus(id)
        ON DELETE CASCADE
);
