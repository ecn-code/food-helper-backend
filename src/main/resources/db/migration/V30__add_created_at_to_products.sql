ALTER TABLE products
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;

UPDATE products
SET created_at = COALESCE(created_at, now());

ALTER TABLE products
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN created_at SET NOT NULL;
