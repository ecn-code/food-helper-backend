CREATE TABLE IF NOT EXISTS coupon_definitions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    condition_description TEXT NOT NULL,
    rule_code VARCHAR(80) NOT NULL,
    reward_amount NUMERIC(10,2) NOT NULL CHECK (reward_amount >= 0),
    period_days INTEGER NOT NULL CHECK (period_days >= 0)
);

CREATE TABLE IF NOT EXISTS challenge_definitions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    reward_amount NUMERIC(10,2) NOT NULL CHECK (reward_amount >= 0),
    period_days INTEGER NOT NULL CHECK (period_days >= 0)
);

INSERT INTO coupon_definitions (code, name, condition_description, rule_code, reward_amount, period_days) VALUES
    ('CAPRICHO', 'Capricho', 'No menu validation required', 'ALWAYS', 10.00, 90),
    ('INOVACION', 'Innovacion', 'The menu must include a recipe-derived product created less than one month ago', 'INOVACION', 15.00, 30),
    ('LUXURY', 'Luxury', 'No menu validation required', 'ALWAYS', 50.00, 180),
    ('NO_REPEATED_PRODUCTS', 'No repeated products', 'The menu must fill every planned day with at least 3 products and cannot repeat the same product on the same day or in the same day part across different days', 'NO_REPEATED_PRODUCTS', 15.00, 30),
    ('OUTSIDE', 'Outside', 'No menu validation required', 'ALWAYS', 20.00, 90),
    ('SUSHI', 'Sushi', 'The menu must include product 256', 'SUSHI', 20.00, 60),
    ('VINTAGE', 'Vintage', 'The menu must include a product not used in the last two months', 'VINTAGE', 5.00, 30)
ON CONFLICT (code) DO NOTHING;

INSERT INTO challenge_definitions (code, name, description, reward_amount, period_days) VALUES
    ('QUEUES', 'Queue challenge', 'Prepare five types of cola in numbered containers and write the number-to-cola correspondence on a hidden note. One person fills the containers, then the other fills cups without seeing the number or contents. Drink through a straw and identify each cola.', 10.00, 30),
    ('TOO_GOOD_TO_GO', 'Too Good To Go challenge', 'For one full day, eat only food from Too Good To Go or food bought with a supermarket discount receipt.', 10.00, 30),
    ('FIVE_EURO_DAYS', '5 euro per day challenge', 'Create a five-day menu for two people using only 25 euros. Only low-value staples such as rice may be reused, and no meal may repeat more than twice, except breakfasts.', 20.00, 30),
    ('LOW_CARB_DAY', 'Low carb day', 'For one full day, consume fewer than 50 grams of carbohydrates.', 8.00, 30),
    ('NO_CAFFEINE_DAY', 'No caffeine day', 'Go one full day without caffeine, theine, or carbonated drinks.', 8.00, 30),
    ('CHEESE_DAY', 'Cheese day', 'For one full day, make cheese the main ingredient at breakfast, lunch, and dinner.', 10.00, 30)
ON CONFLICT (code) DO NOTHING;
