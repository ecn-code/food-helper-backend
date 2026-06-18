CREATE TABLE IF NOT EXISTS current_week_menus (
    id BIGSERIAL PRIMARY KEY,
    proposed_week_menu_id BIGINT NOT NULL UNIQUE,
    snapshot_json TEXT NOT NULL,
    CONSTRAINT fk_current_week_menus_proposed_menu
        FOREIGN KEY (proposed_week_menu_id)
        REFERENCES proposed_week_menus(id)
        ON DELETE CASCADE
);
