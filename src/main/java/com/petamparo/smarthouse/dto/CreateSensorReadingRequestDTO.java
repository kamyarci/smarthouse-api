package com.petamparo.smarthouse.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSensorReadingRequestDTO(

        @Size(max = 64)
        @Pattern(regexp = "[A-Za-z0-9_-]+")
        String deviceId,

        @NotNull
        @DecimalMin("-40.0")
        @DecimalMax("80.0")
        BigDecimal temperature,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal humidity,

        @NotNull
        Boolean motionDetected,

        @NotNull
        @PositiveOrZero
        BigDecimal foodWeightKg,


        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal foodPercentage,

        @NotNull
        @PositiveOrZero
        BigDecimal waterWeightKg,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal waterPercentage

) {
}