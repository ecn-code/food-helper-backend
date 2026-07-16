CREATE TABLE IF NOT EXISTS challenge_redemptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_code VARCHAR(80) NOT NULL,
    reward_amount NUMERIC(10,2) NOT NULL,
    used_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_challenge_redemptions_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_challenge_redemptions_user_challenge_used
    ON challenge_redemptions(user_id, challenge_code, used_at DESC, id DESC);
