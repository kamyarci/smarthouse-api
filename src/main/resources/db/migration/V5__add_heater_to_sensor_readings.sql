ALTER TABLE sensor_readings
    ADD COLUMN heater_on BOOLEAN,
    ADD COLUMN heater_mode VARCHAR(8),
    ADD CONSTRAINT chk_sensor_readings_heater_mode
        CHECK (heater_mode IN ('AUTO', 'ON', 'OFF'));
