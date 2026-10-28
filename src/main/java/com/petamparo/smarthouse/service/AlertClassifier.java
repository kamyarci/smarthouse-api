package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.entity.SystemSettingsEntity;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Component
public class AlertClassifier {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    public record Classification(AlertTypeEnum type, AlertSeverity severity, BigDecimal value, String message) {
    }

    public Map<AlertTypeEnum, Classification> classify(SensorReadingEntity reading, SystemSettingsEntity settings) {
        Map<AlertTypeEnum, Classification> result = new EnumMap<>(AlertTypeEnum.class);

        BigDecimal temperature = reading.getTemperature();
        if (temperature != null) {
            AlertSeverity cold = classifyLowTemperature(temperature, settings);
            if (cold != null) {
                result.put(AlertTypeEnum.LOW_TEMPERATURE, new Classification(AlertTypeEnum.LOW_TEMPERATURE, cold,
                        temperature, lowTemperatureMessage(temperature, cold, settings)));
            }
            if (temperature.compareTo(settings.getHighTemperatureAbove()) > 0) {
                result.put(AlertTypeEnum.HIGH_TEMPERATURE, new Classification(AlertTypeEnum.HIGH_TEMPERATURE,
                        settings.getHighTemperatureSeverity(), temperature,
                        "Temperatura alta no abrigo: %s °C (acima do limite configurado de %s °C)"
                                .formatted(format(temperature), format(settings.getHighTemperatureAbove()))));
            }
        }

        addResource(result, AlertTypeEnum.LOW_WATER, "água", reading.getWaterPercentage(),
                settings.getWaterWarningPercent(), settings.getWaterHighPercent(), settings.getWaterCriticalPercent());
        addResource(result, AlertTypeEnum.LOW_FOOD, "ração", reading.getFoodPercentage(),
                settings.getFoodWarningPercent(), settings.getFoodHighPercent(), settings.getFoodCriticalPercent());

        return result;
    }


    AlertSeverity classifyLowTemperature(BigDecimal temperature, SystemSettingsEntity settings) {
        if (temperature.compareTo(settings.getLowTemperatureCriticalBelow()) < 0) {
            return AlertSeverity.CRITICAL;
        }
        if (temperature.compareTo(settings.getLowTemperatureHighBelow()) < 0) {
            return AlertSeverity.HIGH;
        }
        if (temperature.compareTo(settings.getLowTemperatureWarningBelow()) < 0) {
            return AlertSeverity.WARNING;
        }
        return null;
    }

    AlertSeverity classifyResource(BigDecimal percentage, BigDecimal warning, BigDecimal high, BigDecimal critical) {
        if (percentage.compareTo(critical) <= 0) {
            return AlertSeverity.CRITICAL;
        }
        if (percentage.compareTo(high) <= 0) {
            return AlertSeverity.HIGH;
        }
        if (percentage.compareTo(warning) <= 0) {
            return AlertSeverity.WARNING;
        }
        return null;
    }

    private void addResource(Map<AlertTypeEnum, Classification> result, AlertTypeEnum type, String resource,
                             BigDecimal percentage, BigDecimal warning, BigDecimal high, BigDecimal critical) {
        if (percentage == null) {
            return;
        }
        AlertSeverity severity = classifyResource(percentage, warning, high, critical);
        if (severity == null) {
            return;
        }
        String message = severity == AlertSeverity.CRITICAL
                ? "Reservatório de %s vazio: %s%% disponível".formatted(resource, format(percentage))
                : "Nível de %s baixo: %s%% disponível (limite configurado: até %s%%)"
                .formatted(resource, format(percentage), format(severity == AlertSeverity.HIGH ? high : warning));
        result.put(type, new Classification(type, severity, percentage, message));
    }

    private String lowTemperatureMessage(BigDecimal temperature, AlertSeverity severity, SystemSettingsEntity settings) {
        BigDecimal limit = switch (severity) {
            case CRITICAL -> settings.getLowTemperatureCriticalBelow();
            case HIGH -> settings.getLowTemperatureHighBelow();
            case WARNING -> settings.getLowTemperatureWarningBelow();
        };
        String prefix = severity == AlertSeverity.WARNING ? "Temperatura baixa" : "Temperatura muito baixa";
        return "%s no abrigo: %s °C (abaixo do limite configurado de %s °C)"
                .formatted(prefix, format(temperature), format(limit));
    }

    private static String format(BigDecimal value) {
        return String.format(PT_BR, "%.1f", value);
    }
}
