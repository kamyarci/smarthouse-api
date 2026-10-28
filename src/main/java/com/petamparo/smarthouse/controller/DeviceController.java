package com.petamparo.smarthouse.controller;

import com.petamparo.smarthouse.dto.DeviceConfigResponseDTO;
import com.petamparo.smarthouse.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final SettingsService settingsService;

    public DeviceController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/config")
    public DeviceConfigResponseDTO config() {
        return settingsService.getDeviceConfig();
    }
}
