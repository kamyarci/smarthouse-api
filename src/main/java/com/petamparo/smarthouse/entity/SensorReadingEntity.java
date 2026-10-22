package com.petamparo.smarthouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "sensor_readings")
@Getter
@Setter
public class SensorReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "humidity")
    private BigDecimal humidity;

    @Column(name = "motion_detected", nullable = false)
    private Boolean motionDetected;

    @Column(name = "food_weight_kg")
    private BigDecimal foodWeightKg;

    @Column(name = "food_percentage")
    private BigDecimal foodPercentage;

    @Column(name = "water_weight_kg")
    private BigDecimal waterWeightKg;

    @Column(name = "water_percentage")
    private BigDecimal waterPercentage;

    @Generated(event = EventType.INSERT)
    @Column(name = "recorded_at", insertable = false, updatable = false)
    private OffsetDateTime recordedAt;
}
