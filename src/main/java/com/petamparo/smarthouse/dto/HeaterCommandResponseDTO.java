package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.enums.HeaterMode;

public record HeaterCommandResponseDTO(String deviceId, HeaterMode requestedMode) {
}
