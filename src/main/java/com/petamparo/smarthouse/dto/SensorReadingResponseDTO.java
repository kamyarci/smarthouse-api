package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.entity.SensorReadingEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SensorReadingResponseDTO(
        Long id,
        String deviceId,
        BigDecimal temperature,
        BigDecimal humidity,
        Boolean motionDetected,
        BigDecimal foodWeightKg,
        BigDecimal foodPercentage,
        BigDecimal waterWeightKg,
        BigDecimal waterPercentage,
        OffsetDateTime recordedAt
) {

    public static SensorReadingResponseDTO from(SensorReadingEntity reading) {
        return new SensorReadingResponseDTO(
                reading.getId(),
                reading.getDeviceId(),
                reading.getTemperature(),
                reading.getHumidity(),
                reading.getMotionDetected(),
                reading.getFoodWeightKg(),
                reading.getFoodPercentage(),
                reading.getWaterWeightKg(),
                reading.getWaterPercentage(),
                reading.getRecordedAt()
        );
    }
}
