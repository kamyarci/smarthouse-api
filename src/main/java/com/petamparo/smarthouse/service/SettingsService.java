package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.dto.DeviceConfigResponseDTO;
import com.petamparo.smarthouse.dto.SettingsResponseDTO;
import com.petamparo.smarthouse.dto.UpdateSettingsRequestDTO;
import com.petamparo.smarthouse.entity.SystemSettingsEntity;
import com.petamparo.smarthouse.event.DeviceConfigChangedEvent;
import com.petamparo.smarthouse.exception.InvalidSettingsException;
import com.petamparo.smarthouse.repository.SystemSettingsRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class SettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public SettingsService(SystemSettingsRepository systemSettingsRepository, Clock clock,
                           ApplicationEventPublisher eventPublisher) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public SystemSettingsEntity getCurrent() {
        return systemSettingsRepository.findById(SystemSettingsEntity.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Linha de system_settings ausente (migration V3)"));
    }

    @Transactional(readOnly = true)
    public SettingsResponseDTO get() {
        return SettingsResponseDTO.from(getCurrent());
    }

    @Transactional(readOnly = true)
    public DeviceConfigResponseDTO getDeviceConfig() {
        SystemSettingsEntity settings = getCurrent();
        return new DeviceConfigResponseDTO(settings.getReadingIntervalSeconds(), settings.getUpdatedAt());
    }

    @Transactional
    public SettingsResponseDTO update(UpdateSettingsRequestDTO request) {
        SystemSettingsEntity settings = systemSettingsRepository.findByIdForUpdate(SystemSettingsEntity.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Linha de system_settings ausente (migration V3)"));

        apply(request.lowTemperatureWarningBelow(), settings::setLowTemperatureWarningBelow);
        apply(request.lowTemperatureHighBelow(), settings::setLowTemperatureHighBelow);
        apply(request.lowTemperatureCriticalBelow(), settings::setLowTemperatureCriticalBelow);
        apply(request.highTemperatureAbove(), settings::setHighTemperatureAbove);
        apply(request.highTemperatureSeverity(), settings::setHighTemperatureSeverity);
        apply(request.waterWarningPercent(), settings::setWaterWarningPercent);
        apply(request.waterHighPercent(), settings::setWaterHighPercent);
        apply(request.waterCriticalPercent(), settings::setWaterCriticalPercent);
        apply(request.foodWarningPercent(), settings::setFoodWarningPercent);
        apply(request.foodHighPercent(), settings::setFoodHighPercent);
        apply(request.foodCriticalPercent(), settings::setFoodCriticalPercent);
        apply(request.readingIntervalSeconds(), settings::setReadingIntervalSeconds);

        List<String> errors = validate(settings);
        if (!errors.isEmpty()) {
            throw new InvalidSettingsException(errors);
        }

        settings.setUpdatedAt(OffsetDateTime.now(clock));
        SystemSettingsEntity saved = systemSettingsRepository.save(settings);

        if (request.readingIntervalSeconds() != null) {
            eventPublisher.publishEvent(new DeviceConfigChangedEvent(saved.getReadingIntervalSeconds()));
        }
        return SettingsResponseDTO.from(saved);
    }

    static List<String> validate(SystemSettingsEntity s) {
        List<String> errors = new ArrayList<>();

        if (!(lt(s.getLowTemperatureCriticalBelow(), s.getLowTemperatureHighBelow())
                && lt(s.getLowTemperatureHighBelow(), s.getLowTemperatureWarningBelow()))) {
            errors.add("Temperatura baixa: exige lowTemperatureCriticalBelow < lowTemperatureHighBelow < lowTemperatureWarningBelow");
        }
        if (!lt(s.getLowTemperatureWarningBelow(), s.getHighTemperatureAbove())) {
            errors.add("highTemperatureAbove deve ser maior que lowTemperatureWarningBelow");
        }
        validateResource(errors, "water", s.getWaterCriticalPercent(), s.getWaterHighPercent(), s.getWaterWarningPercent());
        validateResource(errors, "food", s.getFoodCriticalPercent(), s.getFoodHighPercent(), s.getFoodWarningPercent());

        return errors;
    }

    private static void validateResource(List<String> errors, String prefix,
                                         BigDecimal critical, BigDecimal high, BigDecimal warning) {
        if (!(lt(critical, high) && lt(high, warning))) {
            errors.add("%s: exige %sCriticalPercent < %sHighPercent < %sWarningPercent"
                    .formatted(prefix, prefix, prefix, prefix));
        }
    }

    private static boolean lt(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) < 0;
    }

    private static <T> void apply(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
