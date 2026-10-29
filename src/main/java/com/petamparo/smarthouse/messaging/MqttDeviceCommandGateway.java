package com.petamparo.smarthouse.messaging;

import com.petamparo.smarthouse.enums.HeaterMode;
import com.petamparo.smarthouse.exception.DeviceUnavailableException;
import com.petamparo.smarthouse.service.DeviceCommandGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "smarthouse.mqtt.enabled", havingValue = "true")
public class MqttDeviceCommandGateway implements DeviceCommandGateway {

    private final MqttConnection mqttConnection;
    private final ObjectMapper objectMapper;
    private final String heaterTopicTemplate;

    public MqttDeviceCommandGateway(MqttConnection mqttConnection,
                                    ObjectMapper objectMapper,
                                    @Value("${smarthouse.mqtt.heater-topic}") String heaterTopicTemplate) {
        this.mqttConnection = mqttConnection;
        this.objectMapper = objectMapper;
        this.heaterTopicTemplate = heaterTopicTemplate;
    }

    @Override
    public void sendHeaterMode(String deviceId, HeaterMode mode) {
        String topic = heaterTopicTemplate.formatted(deviceId);
        String payload = objectMapper.writeValueAsString(Map.of("mode", mode.name()));

        if (!mqttConnection.publish(topic, payload, true)) {
            throw new DeviceUnavailableException("Broker MQTT indisponível; comando não enviado a " + deviceId);
        }
    }
}
