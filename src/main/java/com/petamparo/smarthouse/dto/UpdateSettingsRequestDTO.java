package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.enums.AlertSeverity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateSettingsRequestDTO(

        @DecimalMin("-40.0") @DecimalMax("80.0")
        BigDecimal lowTemperatureWarningBelow,

        @DecimalMin("-40.0") @DecimalMax("80.0")
        BigDecimal lowTemperatureHighBelow,

        @DecimalMin("-40.0") @DecimalMax("80.0")
        BigDecimal lowTemperatureCriticalBelow,

        @DecimalMin("-40.0") @DecimalMax("80.0")
        BigDecimal highTemperatureAbove,

        AlertSeverity highTemperatureSeverity,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal waterWarningPercent,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal waterHighPercent,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal waterCriticalPercent,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal foodWarningPercent,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal foodHighPercent,

        @DecimalMin("0.0") @DecimalMax("100.0")
        BigDecimal foodCriticalPercent,

        @Min(5) @Max(3600)
        Integer readingIntervalSeconds
) {
}