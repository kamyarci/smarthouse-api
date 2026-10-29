package com.petamparo.smarthouse.controller;

import com.petamparo.smarthouse.dto.DeviceConfigResponseDTO;
import com.petamparo.smarthouse.dto.HeaterCommandRequestDTO;
import com.petamparo.smarthouse.dto.HeaterCommandResponseDTO;
import com.petamparo.smarthouse.service.HeaterCommandService;
import com.petamparo.smarthouse.service.SettingsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final SettingsService settingsService;
    private final HeaterCommandService heaterCommandService;

    public DeviceController(SettingsService settingsService, HeaterCommandService heaterCommandService) {
        this.settingsService = settingsService;
        this.heaterCommandService = heaterCommandService;
    }

    @GetMapping("/config")
    public DeviceConfigResponseDTO config() {
        return settingsService.getDeviceConfig();
    }

    @PatchMapping("/{deviceId}/heater")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public HeaterCommandResponseDTO changeHeaterMode(
            @PathVariable @Size(max = 64) @Pattern(regexp = "[A-Za-z0-9_-]+") String deviceId,
            @Valid @RequestBody HeaterCommandRequestDTO request) {
        return heaterCommandService.changeMode(deviceId, request.mode());
    }
}
