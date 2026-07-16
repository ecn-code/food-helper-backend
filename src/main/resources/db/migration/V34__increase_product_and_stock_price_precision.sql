ALTER TABLE products
    ALTER COLUMN default_price TYPE NUMERIC(12,4);

ALTER TABLE stock_entries
    ALTER COLUMN price TYPE NUMERIC(12,4);

ALTER TABLE stock_movements
    ALTER COLUMN price TYPE NUMERIC(14,4);
