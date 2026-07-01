ALTER TABLE proposed_week_menu_products
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE proposed_week_menu_products
    ALTER COLUMN units DROP NOT NULL;

ALTER TABLE proposed_week_menu_products
    ALTER COLUMN grams DROP NOT NULL;

ALTER TABLE proposed_week_menu_products
    ADD COLUMN IF NOT EXISTS product_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS calories NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS carbohydrates NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS proteins NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS fats NUMERIC(10,2);

UPDATE proposed_week_menu_products p
SET product_name = COALESCE(p.product_name, pr.name),
    calories = COALESCE(p.calories, nv.calories),
    carbohydrates = COALESCE(p.carbohydrates, nv.carbohydrates),
    proteins = COALESCE(p.proteins, nv.proteins),
    fats = COALESCE(p.fats, nv.fats)
FROM products pr
LEFT JOIN nutritional_values nv ON nv.product_id = pr.id
WHERE p.product_id = pr.id;
