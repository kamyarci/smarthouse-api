CREATE TABLE system_settings
(
    id                       INTEGER PRIMARY KEY,
    low_temp_warning_below   NUMERIC(5, 2)            NOT NULL,
    low_temp_high_below      NUMERIC(5, 2)            NOT NULL,
    low_temp_critical_below  NUMERIC(5, 2)            NOT NULL,
    high_temp_above          NUMERIC(5, 2)            NOT NULL,
    high_temp_severity       VARCHAR(16)              NOT NULL,
    water_warning_percent    NUMERIC(5, 2)            NOT NULL,
    water_high_percent       NUMERIC(5, 2)            NOT NULL,
    water_critical_percent   NUMERIC(5, 2)            NOT NULL,
    food_warning_percent     NUMERIC(5, 2)            NOT NULL,
    food_high_percent        NUMERIC(5, 2)            NOT NULL,
    food_critical_percent    NUMERIC(5, 2)            NOT NULL,
    reading_interval_seconds INTEGER                  NOT NULL,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_settings_single_row CHECK (id = 1),
    CONSTRAINT chk_settings_temperature_order
        CHECK (low_temp_critical_below < low_temp_high_below
            AND low_temp_high_below < low_temp_warning_below
            AND low_temp_warning_below < high_temp_above),
    CONSTRAINT chk_settings_high_temp_severity
        CHECK (high_temp_severity IN ('WARNING', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_settings_water_order
        CHECK (water_critical_percent >= 0
            AND water_critical_percent < water_high_percent
            AND water_high_percent < water_warning_percent
            AND water_warning_percent <= 100),
    CONSTRAINT chk_settings_food_order
        CHECK (food_critical_percent >= 0
            AND food_critical_percent < food_high_percent
            AND food_high_percent < food_warning_percent
            AND food_warning_percent <= 100),
    CONSTRAINT chk_settings_interval
        CHECK (reading_interval_seconds BETWEEN 5 AND 3600)
);

INSERT INTO system_settings (id,
                             low_temp_warning_below, low_temp_high_below, low_temp_critical_below,
                             high_temp_above, high_temp_severity,
                             water_warning_percent, water_high_percent, water_critical_percent,
                             food_warning_percent, food_high_percent, food_critical_percent,
                             reading_interval_seconds)
VALUES (1, 15, 5, 0, 35, 'HIGH', 20, 10, 0, 20, 10, 0, 10);
