package com.petamparo.smarthouse.dto;

import jakarta.validation.constraints.Size;

public record AlertActionRequestDTO(

        @Size(max = 100)
        String volunteerName,

        @Size(max = 500)
        String note
) {
}
