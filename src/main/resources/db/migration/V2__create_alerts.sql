CREATE TABLE alerts
(
    id                BIGSERIAL PRIMARY KEY,
    device_id         VARCHAR(64)              NOT NULL,
    sensor_reading_id BIGINT REFERENCES sensor_readings (id),
    type              VARCHAR(32)              NOT NULL,
    severity          VARCHAR(16)              NOT NULL,
    status            VARCHAR(16)              NOT NULL,
    message           VARCHAR(500)             NOT NULL,
    trigger_value     NUMERIC(8, 2),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at   TIMESTAMP WITH TIME ZONE,
    resolved_at       TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_alerts_type
        CHECK (type IN ('LOW_TEMPERATURE', 'HIGH_TEMPERATURE', 'LOW_WATER', 'LOW_FOOD')),
    CONSTRAINT chk_alerts_severity
        CHECK (severity IN ('WARNING', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_alerts_status
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED')),
    CONSTRAINT chk_alerts_resolved_at
        CHECK ((status = 'RESOLVED') = (resolved_at IS NOT NULL))
);

CREATE UNIQUE INDEX uq_alerts_active_device_type
    ON alerts (device_id, type)
    WHERE status <> 'RESOLVED';

CREATE INDEX idx_alerts_created_at ON alerts (created_at DESC);
CREATE INDEX idx_alerts_sensor_reading ON alerts (sensor_reading_id);

CREATE TABLE alert_events
(
    id                BIGSERIAL PRIMARY KEY,
    alert_id          BIGINT                   NOT NULL REFERENCES alerts (id),
    sensor_reading_id BIGINT REFERENCES sensor_readings (id),
    event_type        VARCHAR(32)              NOT NULL,
    previous_severity VARCHAR(16),
    new_severity      VARCHAR(16),
    previous_status   VARCHAR(16),
    new_status        VARCHAR(16),
    actor             VARCHAR(100),
    note              VARCHAR(500),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_alert_events_alert ON alert_events (alert_id, created_at);
