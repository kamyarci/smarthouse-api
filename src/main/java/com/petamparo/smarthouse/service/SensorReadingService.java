package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.dto.CreateSensorReadingRequestDTO;
import com.petamparo.smarthouse.dto.PageResponseDTO;
import com.petamparo.smarthouse.dto.SensorReadingResponseDTO;
import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.exception.ResourceNotFoundException;
import com.petamparo.smarthouse.repository.SensorReadingRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final AlertService alertService;
    private final String defaultDeviceId;

    public SensorReadingService(SensorReadingRepository sensorReadingRepository,
                                AlertService alertService,
                                @Value("${smarthouse.default-device-id}") String defaultDeviceId) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.alertService = alertService;
        this.defaultDeviceId = defaultDeviceId;
    }

    @Transactional
    public SensorReadingResponseDTO create(CreateSensorReadingRequestDTO request) {
        SensorReadingEntity sensorReading = new SensorReadingEntity();

        sensorReading.setDeviceId(resolveDeviceId(request.deviceId()));
        sensorReading.setTemperature(request.temperature());
        sensorReading.setHumidity(request.humidity());
        sensorReading.setMotionDetected(request.motionDetected());
        sensorReading.setFoodWeightKg(request.foodWeightKg());
        sensorReading.setFoodPercentage(request.foodPercentage());
        sensorReading.setWaterWeightKg(request.waterWeightKg());
        sensorReading.setWaterPercentage(request.waterPercentage());

        SensorReadingEntity saved = sensorReadingRepository.save(sensorReading);
        alertService.evaluate(saved);
        return SensorReadingResponseDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public Optional<SensorReadingEntity> findLatestEntity(String deviceId) {
        return sensorReadingRepository.findFirstByDeviceIdOrderByRecordedAtDescIdDesc(resolveDeviceId(deviceId));
    }

    @Transactional(readOnly = true)
    public SensorReadingResponseDTO findLatest(String deviceId) {
        return findLatestEntity(deviceId)
                .map(SensorReadingResponseDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma leitura registrada"));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<SensorReadingResponseDTO> search(String deviceId, OffsetDateTime from, OffsetDateTime to,
                                                            int page, int size) {
        Specification<SensorReadingEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (deviceId != null) predicates.add(cb.equal(root.get("deviceId"), deviceId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("recordedAt"), from));
            if (to != null) predicates.add(cb.lessThan(root.get("recordedAt"), to));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("recordedAt"), Sort.Order.desc("id")));
        return PageResponseDTO.from(sensorReadingRepository.findAll(spec, pageRequest), SensorReadingResponseDTO::from);
    }

    public String resolveDeviceId(String deviceId) {
        return deviceId == null || deviceId.isBlank() ? defaultDeviceId : deviceId;
    }
}
