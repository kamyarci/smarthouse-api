package com.petamparo.smarthouse.dto;

import com.petamparo.smarthouse.enums.HeaterMode;
import jakarta.validation.constraints.NotNull;

public record HeaterCommandRequestDTO(@NotNull HeaterMode mode) {
}
