package com.petamparo.smarthouse.controller;

import com.petamparo.smarthouse.dto.SettingsResponseDTO;
import com.petamparo.smarthouse.dto.UpdateSettingsRequestDTO;
import com.petamparo.smarthouse.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponseDTO get() {
        return settingsService.get();
    }

    @PatchMapping
    public SettingsResponseDTO update(@Valid @RequestBody UpdateSettingsRequestDTO request) {
        return settingsService.update(request);
    }
}
