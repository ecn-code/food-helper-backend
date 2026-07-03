ALTER TABLE recipe_product_origins
    ADD COLUMN IF NOT EXISTS stock_from_composition BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE recipe_product_origins
SET stock_from_composition = TRUE
WHERE stock_from_composition IS NULL;
