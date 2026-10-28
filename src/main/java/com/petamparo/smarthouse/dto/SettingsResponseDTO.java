package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.entity.SystemSettingsEntity;
import com.petamparo.smarthouse.enums.AlertSeverity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettingsResponseDTO(
        BigDecimal lowTemperatureWarningBelow,
        BigDecimal lowTemperatureHighBelow,
        BigDecimal lowTemperatureCriticalBelow,
        BigDecimal highTemperatureAbove,
        AlertSeverity highTemperatureSeverity,
        BigDecimal waterWarningPercent,
        BigDecimal waterHighPercent,
        BigDecimal waterCriticalPercent,
        BigDecimal foodWarningPercent,
        BigDecimal foodHighPercent,
        BigDecimal foodCriticalPercent,
        Integer readingIntervalSeconds,
        OffsetDateTime updatedAt
) {

    public static SettingsResponseDTO from(SystemSettingsEntity settings) {
        return new SettingsResponseDTO(
                settings.getLowTemperatureWarningBelow(),
                settings.getLowTemperatureHighBelow(),
                settings.getLowTemperatureCriticalBelow(),
                settings.getHighTemperatureAbove(),
                settings.getHighTemperatureSeverity(),
                settings.getWaterWarningPercent(),
                settings.getWaterHighPercent(),
                settings.getWaterCriticalPercent(),
                settings.getFoodWarningPercent(),
                settings.getFoodHighPercent(),
                settings.getFoodCriticalPercent(),
                settings.getReadingIntervalSeconds(),
                settings.getUpdatedAt()
        );
    }
}

