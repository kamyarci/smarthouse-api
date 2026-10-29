package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.dto.HeaterCommandResponseDTO;
import com.petamparo.smarthouse.enums.HeaterMode;
import org.springframework.stereotype.Service;

@Service
public class HeaterCommandService {

    private final DeviceCommandGateway deviceCommandGateway;

    public HeaterCommandService(DeviceCommandGateway deviceCommandGateway) {
        this.deviceCommandGateway = deviceCommandGateway;
    }

    public HeaterCommandResponseDTO changeMode(String deviceId, HeaterMode mode) {
        deviceCommandGateway.sendHeaterMode(deviceId, mode);
        return new HeaterCommandResponseDTO(deviceId, mode);
    }
}
