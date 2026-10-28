package com.petamparo.smarthouse.controller;

import com.petamparo.smarthouse.dto.CreateSensorReadingRequestDTO;
import com.petamparo.smarthouse.dto.PageResponseDTO;
import com.petamparo.smarthouse.dto.SensorReadingResponseDTO;
import com.petamparo.smarthouse.service.SensorReadingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    public SensorReadingController(SensorReadingService sensorReadingService) {
        this.sensorReadingService = sensorReadingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorReadingResponseDTO create(@Valid @RequestBody CreateSensorReadingRequestDTO request) {
        return sensorReadingService.create(request);
    }

    @GetMapping("/latest")
    public SensorReadingResponseDTO latest(@RequestParam(required = false) String deviceId) {
        return sensorReadingService.findLatest(deviceId);
    }

    @GetMapping
    public PageResponseDTO<SensorReadingResponseDTO> search(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return sensorReadingService.search(deviceId, from, to, page, size);
    }
}
