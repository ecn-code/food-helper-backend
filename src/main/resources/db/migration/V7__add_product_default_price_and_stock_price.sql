ALTER TABLE products
    ADD COLUMN IF NOT EXISTS default_price NUMERIC(10,2);

ALTER TABLE stock_entries
    ADD COLUMN IF NOT EXISTS price NUMERIC(10,2);

UPDATE stock_entries s
SET price = COALESCE(s.price, p.default_price, 0.00)
FROM products p
WHERE p.id = s.product_id
  AND s.price IS NULL;

ALTER TABLE stock_entries
    ALTER COLUMN price SET NOT NULL;
