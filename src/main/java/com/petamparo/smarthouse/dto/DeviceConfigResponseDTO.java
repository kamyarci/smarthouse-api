package com.petamparo.smarthouse.dto;

import java.time.OffsetDateTime;

public record DeviceConfigResponseDTO(
        int readingIntervalSeconds,
        OffsetDateTime updatedAt
) {
}
