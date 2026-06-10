CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS nutritional_values (
    product_id BIGINT PRIMARY KEY,
    calories NUMERIC(10,2) NOT NULL,
    carbohydrates NUMERIC(10,2) NOT NULL,
    proteins NUMERIC(10,2) NOT NULL,
    fats NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_nutritional_values_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);
