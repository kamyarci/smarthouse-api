package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.entity.SystemSettingsEntity;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertClassifierTest {

    private final AlertClassifier classifier = new AlertClassifier();
    private SystemSettingsEntity settings;

    @BeforeEach
    void setUp() {
        settings = new SystemSettingsEntity();
        settings.setLowTemperatureWarningBelow(new BigDecimal("15"));
        settings.setLowTemperatureHighBelow(new BigDecimal("5"));
        settings.setLowTemperatureCriticalBelow(new BigDecimal("0"));
        settings.setHighTemperatureAbove(new BigDecimal("35"));
        settings.setHighTemperatureSeverity(AlertSeverity.HIGH);
        settings.setWaterWarningPercent(new BigDecimal("20"));
        settings.setWaterHighPercent(new BigDecimal("10"));
        settings.setWaterCriticalPercent(new BigDecimal("0"));
        settings.setFoodWarningPercent(new BigDecimal("20"));
        settings.setFoodHighPercent(new BigDecimal("10"));
        settings.setFoodCriticalPercent(new BigDecimal("0"));
    }

    @ParameterizedTest(name = "{0} °C -> {1}")
    @CsvSource(nullValues = "NONE", value = {
            "-15.8, CRITICAL",
            "-0.1,  CRITICAL",
            "0,     HIGH",
            "4.4,   HIGH",
            "4.99,  HIGH",
            "5,     WARNING",
            "14.9,  WARNING",
            "15,    NONE",
            "25,    NONE"
    })
    void classificaTemperaturaBaixa(BigDecimal temperature, AlertSeverity expected) {
        assertThat(classifier.classifyLowTemperature(temperature, settings)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}% -> {1}")
    @CsvSource(nullValues = "NONE", value = {
            "0,    CRITICAL",
            "0.1,  HIGH",
            "8,    HIGH",
            "10,   HIGH",
            "10.1, WARNING",
            "18,   WARNING",
            "20,   WARNING",
            "20.1, NONE",
            "60,   NONE"
    })
    void classificaNivelDeRecurso(BigDecimal percentage, AlertSeverity expected) {
        AlertSeverity severity = classifier.classifyResource(percentage,
                settings.getWaterWarningPercent(), settings.getWaterHighPercent(), settings.getWaterCriticalPercent());

        assertThat(severity).isEqualTo(expected);
    }

    @Test
    void temperaturaExatamenteNoLimiteAltoNaoGeraAlerta() {
        Map<AlertTypeEnum, AlertClassifier.Classification> result = classifier.classify(reading("35", "60", "60"), settings);

        assertThat(result).doesNotContainKey(AlertTypeEnum.HIGH_TEMPERATURE);
    }

    @Test
    void temperaturaAcimaDoLimiteUsaSeveridadeConfigurada() {
        settings.setHighTemperatureSeverity(AlertSeverity.CRITICAL);

        Map<AlertTypeEnum, AlertClassifier.Classification> result = classifier.classify(reading("35.1", "60", "60"), settings);

        assertThat(result.get(AlertTypeEnum.HIGH_TEMPERATURE).severity()).isEqualTo(AlertSeverity.CRITICAL);
    }

    @Test
    void aguaERacaoGeramAlertasIndependentes() {
        Map<AlertTypeEnum, AlertClassifier.Classification> result = classifier.classify(reading("25", "8", "18"), settings);

        assertThat(result).containsOnlyKeys(AlertTypeEnum.LOW_WATER, AlertTypeEnum.LOW_FOOD);
        assertThat(result.get(AlertTypeEnum.LOW_WATER).severity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(result.get(AlertTypeEnum.LOW_FOOD).severity()).isEqualTo(AlertSeverity.WARNING);
    }

    @Test
    void leituraNormalNaoGeraAlertas() {
        assertThat(classifier.classify(reading("22", "60", "60"), settings)).isEmpty();
    }

    @Test
    void valorAusenteNaoGeraAlertaParaAqueleSensor() {
        Map<AlertTypeEnum, AlertClassifier.Classification> result = classifier.classify(reading(null, null, "8"), settings);

        assertThat(result).containsOnlyKeys(AlertTypeEnum.LOW_FOOD);
    }

    @Test
    void alertaGuardaValorQueDisparouEMensagem() {
        AlertClassifier.Classification cold = classifier.classify(reading("4.4", "60", "60"), settings)
                .get(AlertTypeEnum.LOW_TEMPERATURE);

        assertThat(cold.value()).isEqualByComparingTo("4.4");
        assertThat(cold.message()).contains("4,4 °C").contains("5,0 °C");
    }

    private static SensorReadingEntity reading(String temperature, String waterPercentage, String foodPercentage) {
        SensorReadingEntity reading = new SensorReadingEntity();
        reading.setDeviceId("abrigo-01");
        reading.setTemperature(temperature == null ? null : new BigDecimal(temperature));
        reading.setWaterPercentage(waterPercentage == null ? null : new BigDecimal(waterPercentage));
        reading.setFoodPercentage(foodPercentage == null ? null : new BigDecimal(foodPercentage));
        reading.setMotionDetected(false);
        return reading;
    }
}
