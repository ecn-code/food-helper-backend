CREATE TABLE IF NOT EXISTS proposed_week_menu_day_parts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    sort_order INTEGER NOT NULL
);

ALTER TABLE proposed_week_menu_sections
    ADD COLUMN IF NOT EXISTS day_part_id BIGINT;

ALTER TABLE proposed_week_menu_sections
    ADD COLUMN IF NOT EXISTS description TEXT NOT NULL DEFAULT '';

INSERT INTO proposed_week_menu_day_parts (name, description, sort_order)
SELECT DISTINCT name, '', sort_order
FROM proposed_week_menu_sections
ON CONFLICT DO NOTHING;

UPDATE proposed_week_menu_sections section
SET day_part_id = day_part.id
FROM proposed_week_menu_day_parts day_part
WHERE section.day_part_id IS NULL
  AND section.name = day_part.name
  AND section.sort_order = day_part.sort_order;

ALTER TABLE proposed_week_menu_sections
    ALTER COLUMN day_part_id SET NOT NULL;

ALTER TABLE proposed_week_menu_sections
    DROP CONSTRAINT IF EXISTS uq_proposed_week_menu_sections_order;

ALTER TABLE proposed_week_menu_sections
    ADD CONSTRAINT fk_proposed_week_menu_sections_day_part
        FOREIGN KEY (day_part_id)
        REFERENCES proposed_week_menu_day_parts(id);

ALTER TABLE proposed_week_menu_sections
    ADD CONSTRAINT uq_proposed_week_menu_sections_day_part UNIQUE (day_id, day_part_id);

CREATE INDEX IF NOT EXISTS idx_proposed_week_menu_day_parts_sort_order ON proposed_week_menu_day_parts(sort_order);
