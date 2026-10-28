package com.petamparo.smarthouse.entity;

import com.petamparo.smarthouse.enums.AlertEventType;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "alert_events")
@Getter
@Setter
public class AlertEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertEntity alert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_reading_id")
    private SensorReadingEntity sensorReading;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AlertEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_severity")
    private AlertSeverity previousSeverity;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_severity")
    private AlertSeverity newSeverity;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private AlertStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private AlertStatus newStatus;

    @Column(name = "actor")
    private String actor;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

}
