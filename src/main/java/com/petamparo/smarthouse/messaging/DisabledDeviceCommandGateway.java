package com.petamparo.smarthouse.messaging;

import com.petamparo.smarthouse.enums.HeaterMode;
import com.petamparo.smarthouse.exception.DeviceUnavailableException;
import com.petamparo.smarthouse.service.DeviceCommandGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "smarthouse.mqtt.enabled", havingValue = "false")
public class DisabledDeviceCommandGateway implements DeviceCommandGateway {

    @Override
    public void sendHeaterMode(String deviceId, HeaterMode mode) {
        throw new DeviceUnavailableException("MQTT desabilitado; não há como enviar comandos aos dispositivos");
    }
}
