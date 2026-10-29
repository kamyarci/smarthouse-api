-- Apaga leituras e alertas para começar a demonstração do zero.
-- Mantém as configurações (system_settings) e o histórico do Flyway.
-- Uso: psql -U postgres -d smarthouse -f scripts/reset-demo.sql
TRUNCATE TABLE alert_events, alerts, sensor_readings RESTART IDENTITY;
