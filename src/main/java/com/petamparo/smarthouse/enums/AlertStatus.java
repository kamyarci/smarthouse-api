package com.petamparo.smarthouse.enums;

import java.util.List;

public enum AlertStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED;

    public static final List<AlertStatus> ACTIVE = List.of(PENDING, IN_PROGRESS);
}
