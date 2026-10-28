package com.petamparo.smarthouse.controller;

import com.petamparo.smarthouse.dto.AlertActionRequestDTO;
import com.petamparo.smarthouse.dto.AlertEventResponseDTO;
import com.petamparo.smarthouse.dto.AlertResponseDTO;
import com.petamparo.smarthouse.dto.PageResponseDTO;
import com.petamparo.smarthouse.enums.AlertSeverity;
import com.petamparo.smarthouse.enums.AlertStatus;
import com.petamparo.smarthouse.enums.AlertTypeEnum;
import com.petamparo.smarthouse.service.AlertService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public PageResponseDTO<AlertResponseDTO> search(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertTypeEnum type,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return alertService.search(deviceId, status, type, severity, from, to, page, size);
    }

    @GetMapping("/active")
    public List<AlertResponseDTO> active(@RequestParam(required = false) String deviceId) {
        return alertService.findActive(deviceId);
    }

    @GetMapping("/{id}/events")
    public List<AlertEventResponseDTO> events(@PathVariable Long id) {
        return alertService.findEvents(id);
    }

    @PatchMapping("/{id}/acknowledge")
    public AlertResponseDTO acknowledge(@PathVariable Long id,
                                        @Valid @RequestBody(required = false) AlertActionRequestDTO request) {
        return alertService.acknowledge(id, request);
    }

    @PatchMapping("/{id}/resolve")
    public AlertResponseDTO resolve(@PathVariable Long id,
                                    @Valid @RequestBody(required = false) AlertActionRequestDTO request) {
        return alertService.resolveManually(id, request);
    }
}
