package com.petamparo.smarthouse.messaging;

import com.petamparo.smarthouse.dto.CreateSensorReadingRequestDTO;
import com.petamparo.smarthouse.dto.SensorReadingResponseDTO;
import com.petamparo.smarthouse.service.SensorReadingService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "smarthouse.mqtt.enabled", havingValue = "true")
public class SensorReadingMqttListener {

    private static final Logger log = LoggerFactory.getLogger(SensorReadingMqttListener.class);

    private final MqttConnection mqttConnection;
    private final SensorReadingService sensorReadingService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final String topic;

    public SensorReadingMqttListener(MqttConnection mqttConnection,
                                     SensorReadingService sensorReadingService,
                                     ObjectMapper objectMapper,
                                     Validator validator,
                                     @Value("${smarthouse.mqtt.topic}") String topic) {
        this.mqttConnection = mqttConnection;
        this.sensorReadingService = sensorReadingService;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.topic = topic;
    }

    @PostConstruct
    void register() {
        mqttConnection.subscribe(topic, this::handle);
    }

    void handle(String topic, MqttMessage message) {
        try {
            CreateSensorReadingRequestDTO request =
                    objectMapper.readValue(message.getPayload(), CreateSensorReadingRequestDTO.class);

            Set<ConstraintViolation<CreateSensorReadingRequestDTO>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                log.warn("Leitura inválida em {}: {}", topic, violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", ")));
                return;
            }

            SensorReadingResponseDTO saved = sensorReadingService.create(request);
            log.debug("Leitura {} de {} salva via MQTT", saved.id(), saved.deviceId());
        } catch (JacksonException e) {
            log.warn("JSON inválido em {}: {}", topic, e.getOriginalMessage());
        } catch (Exception e) {
            log.error("Falha ao processar leitura recebida em {}", topic, e);
        }
    }
}
