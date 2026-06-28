CREATE TABLE IF NOT EXISTS money_boxes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    user_id BIGINT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_money_boxes_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_manual_money_boxes_name_lower
    ON money_boxes (LOWER(name))
    WHERE user_id IS NULL;

INSERT INTO money_boxes (name, user_id)
SELECT username, id
FROM app_users
ON CONFLICT (user_id) DO NOTHING;

ALTER TABLE user_money_movements
    ADD COLUMN IF NOT EXISTS money_box_id BIGINT;

UPDATE user_money_movements movement
SET money_box_id = money_box.id
FROM money_boxes money_box
WHERE money_box.user_id = movement.user_id
  AND movement.money_box_id IS NULL;

ALTER TABLE user_money_movements
    ALTER COLUMN money_box_id SET NOT NULL,
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE user_money_movements
    ADD CONSTRAINT fk_user_money_movements_money_box
        FOREIGN KEY (money_box_id)
        REFERENCES money_boxes(id)
        ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_user_money_movements_box_created
    ON user_money_movements(money_box_id, created_at DESC, id DESC);

CREATE OR REPLACE FUNCTION create_user_money_box()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO money_boxes (name, user_id)
    VALUES (NEW.username, NEW.id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_create_user_money_box ON app_users;

CREATE TRIGGER trg_create_user_money_box
AFTER INSERT ON app_users
FOR EACH ROW
EXECUTE FUNCTION create_user_money_box();
