package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.dto.AlertActionRequestDTO;
import com.petamparo.smarthouse.dto.AlertEventResponseDTO;
import com.petamparo.smarthouse.dto.AlertResponseDTO;
import com.petamparo.smarthouse.dto.PageResponseDTO;
import com.petamparo.smarthouse.entity.AlertEntity;
import com.petamparo.smarthouse.entity.AlertEventEntity;
import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.enums.AlertEventType;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertStatus;
import com.petamparo.smarthouse.enums.AlertTypeEnum;
import com.petamparo.smarthouse.exception.ConflictException;
import com.petamparo.smarthouse.exception.ResourceNotFoundException;
import com.petamparo.smarthouse.repository.AlertEventRepository;
import com.petamparo.smarthouse.repository.AlertRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertEventRepository alertEventRepository;
    private final AlertClassifier alertClassifier;
    private final SettingsService settingsService;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public AlertService(AlertRepository alertRepository,
                        AlertEventRepository alertEventRepository,
                        AlertClassifier alertClassifier,
                        SettingsService settingsService,
                        JdbcTemplate jdbcTemplate,
                        Clock clock) {
        this.alertRepository = alertRepository;
        this.alertEventRepository = alertEventRepository;
        this.alertClassifier = alertClassifier;
        this.settingsService = settingsService;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional
    public void evaluate(SensorReadingEntity reading) {
        String deviceId = reading.getDeviceId();

        jdbcTemplate.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> null, "alerts:" + deviceId);

        Map<AlertTypeEnum, AlertClassifier.Classification> conditions =
                alertClassifier.classify(reading, settingsService.getCurrent());

        Map<AlertTypeEnum, AlertEntity> activeByType = new EnumMap<>(AlertTypeEnum.class);
        for (AlertEntity alert : alertRepository.findForUpdate(deviceId, AlertStatus.ACTIVE)) {
            activeByType.put(alert.getType(), alert);
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        for (AlertTypeEnum type : AlertTypeEnum.values()) {
            AlertClassifier.Classification condition = conditions.get(type);
            AlertEntity active = activeByType.get(type);

            if (condition != null && active == null) {
                create(reading, condition, now);
            } else if (condition != null && active.getSeverity() != condition.severity()) {
                changeSeverity(active, reading, condition, now);
            } else if (condition == null && active != null) {
                resolve(active, reading, AlertEventType.AUTO_RESOLVED, null, "Condição normalizada", now);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<AlertResponseDTO> findActive(String deviceId) {
        List<AlertEntity> alerts = deviceId == null
                ? alertRepository.findByStatusInOrderByCreatedAtDesc(AlertStatus.ACTIVE)
                : alertRepository.findByDeviceIdAndStatusInOrderByCreatedAtDesc(deviceId, AlertStatus.ACTIVE);
        return alerts.stream().map(AlertResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<AlertResponseDTO> search(String deviceId, AlertStatus status, AlertTypeEnum type,
                                                    AlertSeverity severity, OffsetDateTime from, OffsetDateTime to,
                                                    int page, int size) {
        Specification<AlertEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (deviceId != null) predicates.add(cb.equal(root.get("deviceId"), deviceId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (severity != null) predicates.add(cb.equal(root.get("severity"), severity));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) predicates.add(cb.lessThan(root.get("createdAt"), to));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        return PageResponseDTO.from(alertRepository.findAll(spec, pageRequest), AlertResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public List<AlertEventResponseDTO> findEvents(Long alertId) {
        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException("Alerta %d não encontrado".formatted(alertId));
        }
        return alertEventRepository.findByAlertIdOrderByCreatedAtAscIdAsc(alertId).stream()
                .map(AlertEventResponseDTO::from)
                .toList();
    }

    @Transactional
    public AlertResponseDTO acknowledge(Long id, AlertActionRequestDTO request) {
        AlertEntity alert = lockAlert(id);
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new ConflictException("Alerta %d já foi resolvido".formatted(id));
        }
        if (alert.getStatus() == AlertStatus.IN_PROGRESS) {
            return AlertResponseDTO.from(alert);
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        AlertEventEntity event = newEvent(alert, null, AlertEventType.ACKNOWLEDGED, now);
        event.setPreviousStatus(alert.getStatus());
        event.setNewStatus(AlertStatus.IN_PROGRESS);
        applyActor(event, request);

        alert.setStatus(AlertStatus.IN_PROGRESS);
        alert.setAcknowledgedAt(now);
        alert.setUpdatedAt(now);
        alertEventRepository.save(event);
        return AlertResponseDTO.from(alert);
    }

    @Transactional
    public AlertResponseDTO resolveManually(Long id, AlertActionRequestDTO request) {
        AlertEntity alert = lockAlert(id);
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new ConflictException("Alerta %d já foi resolvido".formatted(id));
        }
        String actor = request != null ? request.volunteerName() : null;
        String note = request != null ? request.note() : null;
        resolve(alert, null, AlertEventType.MANUALLY_RESOLVED, actor, note, OffsetDateTime.now(clock));
        return AlertResponseDTO.from(alert);
    }

    private AlertEntity lockAlert(Long id) {
        return alertRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta %d não encontrado".formatted(id)));
    }

    private void create(SensorReadingEntity reading, AlertClassifier.Classification condition, OffsetDateTime now) {
        AlertEntity alert = new AlertEntity();
        alert.setDeviceId(reading.getDeviceId());
        alert.setSensorReading(reading);
        alert.setType(condition.type());
        alert.setSeverity(condition.severity());
        alert.setStatus(AlertStatus.PENDING);
        alert.setMessage(condition.message());
        alert.setTriggerValue(condition.value());
        alert.setCreatedAt(now);
        alert.setUpdatedAt(now);
        alertRepository.save(alert);

        AlertEventEntity event = newEvent(alert, reading, AlertEventType.CREATED, now);
        event.setNewSeverity(condition.severity());
        event.setNewStatus(AlertStatus.PENDING);
        alertEventRepository.save(event);
    }

    private void changeSeverity(AlertEntity alert, SensorReadingEntity reading,
                                AlertClassifier.Classification condition, OffsetDateTime now) {
        AlertEventEntity event = newEvent(alert, reading, AlertEventType.SEVERITY_CHANGED, now);
        event.setPreviousSeverity(alert.getSeverity());
        event.setNewSeverity(condition.severity());

        alert.setSeverity(condition.severity());
        alert.setMessage(condition.message());
        alert.setTriggerValue(condition.value());
        alert.setUpdatedAt(now);
        alertEventRepository.save(event);
    }

    private void resolve(AlertEntity alert, SensorReadingEntity reading, AlertEventType eventType,
                         String actor, String note, OffsetDateTime now) {
        AlertEventEntity event = newEvent(alert, reading, eventType, now);
        event.setPreviousStatus(alert.getStatus());
        event.setNewStatus(AlertStatus.RESOLVED);
        event.setActor(actor);
        event.setNote(note);

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(now);
        alert.setUpdatedAt(now);

        alertRepository.saveAndFlush(alert);
        alertEventRepository.save(event);
    }

    private static AlertEventEntity newEvent(AlertEntity alert, SensorReadingEntity reading,
                                             AlertEventType type, OffsetDateTime now) {
        AlertEventEntity event = new AlertEventEntity();
        event.setAlert(alert);
        event.setSensorReading(reading);
        event.setEventType(type);
        event.setCreatedAt(now);
        return event;
    }

    private static void applyActor(AlertEventEntity event, AlertActionRequestDTO request) {
        if (request != null) {
            event.setActor(request.volunteerName());
            event.setNote(request.note());
        }
    }
}
