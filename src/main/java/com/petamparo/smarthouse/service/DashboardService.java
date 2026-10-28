package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.dto.AlertResponseDTO;
import com.petamparo.smarthouse.dto.DashboardSummaryDTO;
import com.petamparo.smarthouse.dto.SensorReadingResponseDTO;
import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {

    private static final int MISSED_READINGS_BEFORE_STALE = 3;

    private final SensorReadingService sensorReadingService;
    private final AlertService alertService;
    private final SettingsService settingsService;
    private final Clock clock;

    public DashboardService(SensorReadingService sensorReadingService,
                            AlertService alertService,
                            SettingsService settingsService,
                            Clock clock) {
        this.sensorReadingService = sensorReadingService;
        this.alertService = alertService;
        this.settingsService = settingsService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(String requestedDeviceId) {
        String deviceId = sensorReadingService.resolveDeviceId(requestedDeviceId);
        int interval = settingsService.getCurrent().getReadingIntervalSeconds();
        int staleAfter = interval * MISSED_READINGS_BEFORE_STALE;

        Optional<SensorReadingEntity> latest = sensorReadingService.findLatestEntity(deviceId);
        Long secondsSince = latest
                .map(r -> Math.max(0, Duration.between(r.getRecordedAt(), OffsetDateTime.now(clock)).toSeconds()))
                .orElse(null);
        boolean stale = secondsSince == null || secondsSince > staleAfter;

        List<AlertResponseDTO> activeAlerts = alertService.findActive(deviceId);
        AlertSeverity highest = activeAlerts.stream()
                .map(AlertResponseDTO::severity)
                .max(Comparator.naturalOrder())
                .orElse(null);

        boolean motion = latest.map(r -> Boolean.TRUE.equals(r.getMotionDetected())).orElse(false);
        boolean severeTemperature = activeAlerts.stream()
                .anyMatch(a -> (a.type() == AlertTypeEnum.LOW_TEMPERATURE || a.type() == AlertTypeEnum.HIGH_TEMPERATURE)
                        && a.severity().compareTo(AlertSeverity.HIGH) >= 0);

        return new DashboardSummaryDTO(
                deviceId,
                latest.map(SensorReadingResponseDTO::from).orElse(null),
                secondsSince,
                stale,
                staleAfter,
                interval,
                activeAlerts,
                highest,
                motion && severeTemperature && !stale
        );
    }
}
