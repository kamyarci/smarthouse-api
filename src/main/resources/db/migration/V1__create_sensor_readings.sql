CREATE TABLE sensor_readings
(
    id               BIGSERIAL PRIMARY KEY,
    temperature      NUMERIC(5, 2),
    humidity         NUMERIC(5, 2),
    motion_detected  BOOLEAN NOT NULL,
    food_weight_kg   NUMERIC(8, 3),
    food_percentage  NUMERIC(5, 2),
    water_weight_kg  NUMERIC(8, 3),
    water_percentage NUMERIC(5, 2),
    recorded_at      TIMESTAMP WITH TIME ZONE
                             NOT NULL DEFAULT CURRENT_TIMESTAMP
);
