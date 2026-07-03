ALTER TABLE products
    ADD COLUMN IF NOT EXISTS nutrition_basis VARCHAR(20) NOT NULL DEFAULT 'PER_100_GRAMS';

UPDATE products p
SET nutrition_basis = 'PER_UNIT'
FROM recipe_product_origins origin
WHERE origin.product_id = p.id;

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS default_units_produced NUMERIC(10,2);

ALTER TABLE recipe_products
    ADD COLUMN IF NOT EXISTS quantity NUMERIC(10,2);

UPDATE recipe_products
SET quantity = grams
WHERE quantity IS NULL;

ALTER TABLE recipe_products
    ADD COLUMN IF NOT EXISTS quantity_type VARCHAR(10) NOT NULL DEFAULT 'GRAMS';

UPDATE recipe_products
SET quantity_type = 'GRAMS'
WHERE quantity_type IS NULL;

ALTER TABLE recipe_products
    ALTER COLUMN quantity SET NOT NULL;

ALTER TABLE recipe_product_origins
    ADD COLUMN IF NOT EXISTS units_produced NUMERIC(10,2);

UPDATE recipe_product_origins
SET units_produced = ROUND(produced_grams / grams_per_unit, 2)
WHERE units_produced IS NULL;

CREATE TABLE IF NOT EXISTS recipe_derived_product_ingredients (
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL,
    quantity_type VARCHAR(10) NOT NULL,
    CONSTRAINT pk_recipe_derived_product_ingredients PRIMARY KEY (recipe_id, product_id),
    CONSTRAINT fk_recipe_derived_product_ingredients_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_derived_product_ingredients_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

INSERT INTO recipe_derived_product_ingredients (recipe_id, product_id, quantity, quantity_type)
SELECT rp.recipe_id,
       rp.product_id,
       ROUND(rp.quantity / origin.units_produced, 2) AS quantity,
       rp.quantity_type
FROM recipe_products rp
JOIN recipe_product_origins origin ON origin.recipe_id = rp.recipe_id
ON CONFLICT (recipe_id, product_id) DO UPDATE
SET quantity = EXCLUDED.quantity,
    quantity_type = EXCLUDED.quantity_type;

ALTER TABLE recipe_products
    DROP COLUMN IF EXISTS grams;

ALTER TABLE recipe_product_origins
    DROP COLUMN IF EXISTS produced_grams,
    DROP COLUMN IF EXISTS grams_per_unit;

ALTER TABLE proposed_week_menu_recipe_productions
    ADD COLUMN IF NOT EXISTS units NUMERIC(12,2);

UPDATE proposed_week_menu_recipe_productions p
SET units = ROUND(p.produced_grams / origin.units_produced, 2)
FROM recipe_product_origins origin
WHERE origin.recipe_id = p.recipe_id
  AND p.units IS NULL;

ALTER TABLE proposed_week_menu_recipe_productions
    ALTER COLUMN units SET NOT NULL;

ALTER TABLE proposed_week_menu_recipe_productions
    DROP COLUMN IF EXISTS produced_grams;
