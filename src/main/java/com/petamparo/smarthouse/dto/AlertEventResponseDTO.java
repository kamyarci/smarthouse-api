package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.entity.AlertEventEntity;
import com.petamparo.smarthouse.enums.AlertEventType;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertStatus;

import java.time.OffsetDateTime;

public record AlertEventResponseDTO(
        Long id,
        AlertEventType eventType,
        AlertSeverity previousSeverity,
        AlertSeverity newSeverity,
        AlertStatus previousStatus,
        AlertStatus newStatus,
        Long sensorReadingId,
        String actor,
        String note,
        OffsetDateTime createdAt
) {

    public static AlertEventResponseDTO from(AlertEventEntity event) {
        return new AlertEventResponseDTO(
                event.getId(),
                event.getEventType(),
                event.getPreviousSeverity(),
                event.getNewSeverity(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getSensorReading() != null ? event.getSensorReading().getId() : null,
                event.getActor(),
                event.getNote(),
                event.getCreatedAt()
        );
    }
}
