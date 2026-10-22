package com.petamparo.smarthouse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateSensorReadingRequestDTO(

        @NotNull
        BigDecimal temperature,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal humidity,

        @NotNull
        boolean motionDetected,

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
