ALTER TABLE sensor_readings
    ADD COLUMN device_id VARCHAR(64) NOT NULL DEFAULT 'abrigo-01';

CREATE INDEX idx_sensor_readings_device_recorded
    ON sensor_readings (device_id, recorded_at DESC);
