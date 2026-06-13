CREATE TABLE IF NOT EXISTS media (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes INTEGER NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    data BYTEA NOT NULL
);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS media_id BIGINT;

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS media_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_products_media'
          AND table_name = 'products'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_media
                FOREIGN KEY (media_id)
                REFERENCES media(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_recipes_media'
          AND table_name = 'recipes'
    ) THEN
        ALTER TABLE recipes
            ADD CONSTRAINT fk_recipes_media
                FOREIGN KEY (media_id)
                REFERENCES media(id);
    END IF;
END $$;
