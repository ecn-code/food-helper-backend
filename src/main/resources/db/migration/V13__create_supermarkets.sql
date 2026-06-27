CREATE TABLE IF NOT EXISTS supermarkets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_supermarkets_name_lower
    ON supermarkets (LOWER(name));

CREATE TABLE IF NOT EXISTS product_supermarkets (
    product_id BIGINT NOT NULL,
    supermarket_id BIGINT NOT NULL,
    CONSTRAINT pk_product_supermarkets PRIMARY KEY (product_id, supermarket_id),
    CONSTRAINT fk_product_supermarkets_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_product_supermarkets_supermarket
        FOREIGN KEY (supermarket_id)
        REFERENCES supermarkets(id)
);

CREATE INDEX IF NOT EXISTS idx_product_supermarkets_supermarket_id
    ON product_supermarkets(supermarket_id);
