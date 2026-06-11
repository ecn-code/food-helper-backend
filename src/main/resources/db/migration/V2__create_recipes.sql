CREATE TABLE IF NOT EXISTS recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    instructions TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS recipe_products (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    grams NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_recipe_products_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_products_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT uq_recipe_products_assignment UNIQUE (recipe_id, product_id)
);

CREATE TABLE IF NOT EXISTS recipe_product_origins (
    recipe_id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    produced_grams NUMERIC(10,2) NOT NULL,
    grams_per_unit NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_recipe_product_origins_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES recipes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipe_product_origins_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);
