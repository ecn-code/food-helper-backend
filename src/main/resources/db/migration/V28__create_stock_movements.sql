CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    stock_entry_id BIGINT,
    movement_type VARCHAR(30) NOT NULL,
    signed_quantity NUMERIC(12,2) NOT NULL CHECK (signed_quantity <> 0),
    effective_date DATE NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    product_name VARCHAR(150) NOT NULL,
    price NUMERIC(12,2),
    expiration_date DATE,
    entry_date DATE,
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_effective_date ON stock_movements(effective_date DESC, recorded_at DESC, id DESC);

INSERT INTO stock_movements (
    product_id,
    stock_entry_id,
    movement_type,
    signed_quantity,
    effective_date,
    product_name,
    price,
    expiration_date,
    entry_date
)
SELECT
    s.product_id,
    s.id,
    'INITIAL_BALANCE',
    s.quantity,
    s.entry_date,
    p.name,
    s.price,
    s.expiration_date,
    s.entry_date
FROM stock_entries s
INNER JOIN products p ON p.id = s.product_id
WHERE s.quantity <> 0
  AND NOT EXISTS (
    SELECT 1
    FROM stock_movements m
    WHERE m.stock_entry_id = s.id
);
