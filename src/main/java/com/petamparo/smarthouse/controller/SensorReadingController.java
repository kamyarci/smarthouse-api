package com.petamparo.smarthouse.controller;

import com.petamparo.smarthouse.dto.CreateSensorReadingRequestDTO;
import com.petamparo.smarthouse.entity.SensorReadingEntity;
import com.petamparo.smarthouse.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    public SensorReadingController(SensorReadingService sensorReadingService) {
        this.sensorReadingService = sensorReadingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorReadingEntity create(@Valid @RequestBody CreateSensorReadingRequestDTO request) {
        return sensorReadingService.create(request);
    }
}
