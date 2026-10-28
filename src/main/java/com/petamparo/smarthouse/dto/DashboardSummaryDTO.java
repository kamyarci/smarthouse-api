package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.enums.AlertSeverity;

import java.util.List;

public record DashboardSummaryDTO(
        String deviceId,
        SensorReadingResponseDTO latestReading,
        Long secondsSinceLastReading,
        boolean stale,
        int staleAfterSeconds,
        int readingIntervalSeconds,
        List<AlertResponseDTO> activeAlerts,
        AlertSeverity highestActiveSeverity,
        boolean possibleAnimalExposure
) {
}
