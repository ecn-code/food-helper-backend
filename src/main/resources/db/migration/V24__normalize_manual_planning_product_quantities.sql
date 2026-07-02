UPDATE proposed_week_menu_products
SET units = NULL,
    grams = NULL
WHERE product_id IS NULL;

ALTER TABLE proposed_week_menu_products
    ADD CONSTRAINT chk_proposed_week_menu_products_manual_quantity
        CHECK (product_id IS NOT NULL OR (units IS NULL AND grams IS NULL));
