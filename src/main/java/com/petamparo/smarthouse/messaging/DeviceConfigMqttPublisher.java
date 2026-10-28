package com.petamparo.smarthouse.messaging;

import com.petamparo.smarthouse.event.DeviceConfigChangedEvent;
import com.petamparo.smarthouse.service.SettingsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "smarthouse.mqtt.enabled", havingValue = "true")
public class DeviceConfigMqttPublisher {

    private final MqttConnection mqttConnection;
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;
    private final String configTopic;

    public DeviceConfigMqttPublisher(MqttConnection mqttConnection,
                                     SettingsService settingsService,
                                     ObjectMapper objectMapper,
                                     @Value("${smarthouse.mqtt.config-topic}") String configTopic) {
        this.mqttConnection = mqttConnection;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
        this.configTopic = configTopic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeviceConfigChanged(DeviceConfigChangedEvent event) {
        publish(event);
    }

    @EventListener
    public void onMqttConnected(MqttConnectedEvent event) {
        int interval = settingsService.getCurrent().getReadingIntervalSeconds();
        publish(new DeviceConfigChangedEvent(interval));
    }

    private void publish(DeviceConfigChangedEvent config) {
        mqttConnection.publish(configTopic, objectMapper.writeValueAsString(config), true);
    }
}
