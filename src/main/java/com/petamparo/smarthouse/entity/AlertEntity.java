package com.petamparo.smarthouse.entity;

import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertTypeEnum;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.OffsetDateTime;

public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @
    private SensorReadingEntity sensorReading;
    private AlertTypeEnum alertType;
    private AlertSeverity alertSeverity;
    private String message;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;
}
