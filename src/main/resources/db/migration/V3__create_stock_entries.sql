CREATE TABLE IF NOT EXISTS stock_entries (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(10,2) NOT NULL CHECK (quantity >= 0),
    expiration_date DATE,
    entry_date DATE NOT NULL,
    CONSTRAINT fk_stock_entries_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stock_entries_product_id ON stock_entries(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_entries_expiration_date ON stock_entries(expiration_date);
