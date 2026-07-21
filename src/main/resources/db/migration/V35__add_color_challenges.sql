INSERT INTO challenge_definitions (code, name, description, reward_amount, period_days) VALUES
    ('COLOR_DAY', 'Color day challenge', 'For one full day, make every meal revolve around a single color.', 20.00, 30),
    ('WEEK_COLOR', 'Week color challenge', 'From Monday to Friday, make every day revolve around a different color. Do not repeat any color, for a total of five colors.', 100.00, 30)
ON CONFLICT (code) DO NOTHING;
