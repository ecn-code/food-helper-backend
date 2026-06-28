CREATE TABLE IF NOT EXISTS user_weight_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    weight NUMERIC(6, 2) NOT NULL CHECK (weight > 0),
    recorded_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_weight_entries_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_weight_entries_user_recorded
    ON user_weight_entries(user_id, recorded_at, id);
