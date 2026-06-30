ALTER TABLE user_weight_entries
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE user_weight_entries
SET created_at = COALESCE(created_at, recorded_at, now()),
    updated_at = COALESCE(updated_at, created_at, recorded_at, now());

ALTER TABLE user_weight_entries
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN updated_at SET DEFAULT now(),
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;
