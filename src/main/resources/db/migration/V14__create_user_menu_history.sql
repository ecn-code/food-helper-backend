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

CREATE INDEX idx_user_menu_history_person_end_date
    ON user_menu_history(person_id, menu_end_date);
