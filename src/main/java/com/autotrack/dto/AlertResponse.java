package com.autotrack.dto;

import com.autotrack.entity.AlertType;
import java.time.Instant;

public record AlertResponse(
        Long id,
        Long vehicleId,
        String plate,
        AlertType type,
        String message,
        boolean acknowledged,
        Instant createdAt
) {}
