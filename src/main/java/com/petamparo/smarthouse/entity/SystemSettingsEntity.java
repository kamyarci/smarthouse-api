package com.petamparo.smarthouse.entity;

import com.petamparo.smarthouse.enums.AlertSeverity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
public class SystemSettingsEntity {

    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Column(name = "low_temp_warning_below", nullable = false)
    private BigDecimal lowTemperatureWarningBelow;

    @Column(name = "low_temp_high_below", nullable = false)
    private BigDecimal lowTemperatureHighBelow;

    @Column(name = "low_temp_critical_below", nullable = false)
    private BigDecimal lowTemperatureCriticalBelow;

    @Column(name = "high_temp_above", nullable = false)
    private BigDecimal highTemperatureAbove;

    @Enumerated(EnumType.STRING)
    @Column(name = "high_temp_severity", nullable = false)
    private AlertSeverity highTemperatureSeverity;

    @Column(name = "water_warning_percent", nullable = false)
    private BigDecimal waterWarningPercent;

    @Column(name = "water_high_percent", nullable = false)
    private BigDecimal waterHighPercent;

    @Column(name = "water_critical_percent", nullable = false)
    private BigDecimal waterCriticalPercent;

    @Column(name = "food_warning_percent", nullable = false)
    private BigDecimal foodWarningPercent;

    @Column(name = "food_high_percent", nullable = false)
    private BigDecimal foodHighPercent;

    @Column(name = "food_critical_percent", nullable = false)
    private BigDecimal foodCriticalPercent;

    @Column(name = "reading_interval_seconds", nullable = false)
    private Integer readingIntervalSeconds;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
