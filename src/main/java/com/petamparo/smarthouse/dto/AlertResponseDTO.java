package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.entity.AlertEntity;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertStatus;
import com.petamparo.smarthouse.enums.AlertTypeEnum;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AlertResponseDTO(
        Long id,
        String deviceId,
        AlertTypeEnum type,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        BigDecimal triggerValue,
        Long sensorReadingId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime acknowledgedAt,
        OffsetDateTime resolvedAt
) {

    public static AlertResponseDTO from(AlertEntity alert) {
        return new AlertResponseDTO(
                alert.getId(),
                alert.getDeviceId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getTriggerValue(),
                alert.getSensorReading() != null ? alert.getSensorReading().getId() : null,
                alert.getCreatedAt(),
                alert.getUpdatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt()
        );
    }
}