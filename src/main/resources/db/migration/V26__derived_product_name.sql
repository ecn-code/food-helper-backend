ALTER TABLE recipe_product_origins
    ADD COLUMN IF NOT EXISTS derived_product_name VARCHAR(150);

UPDATE recipe_product_origins origin
SET derived_product_name = p.name
FROM products p
WHERE p.id = origin.product_id
  AND origin.derived_product_name IS NULL;

ALTER TABLE recipe_product_origins
    ALTER COLUMN derived_product_name SET NOT NULL;
