ALTER TABLE proposed_week_menus
    ADD COLUMN IF NOT EXISTS users INTEGER NOT NULL DEFAULT 1;

UPDATE proposed_week_menus
SET users = 1
WHERE users IS NULL;
