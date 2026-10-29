package com.petamparo.smarthouse.service;

import com.petamparo.smarthouse.enums.HeaterMode;

public interface DeviceCommandGateway {

    void sendHeaterMode(String deviceId, HeaterMode mode);
}
