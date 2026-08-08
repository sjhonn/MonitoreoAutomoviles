package com.autotrack.dto;

import java.time.Instant;

public record LocationResponse(
        Long id,
        Long vehicleId,
        String plate,
        Double latitude,
        Double longitude,
        Double speed,
        Double heading,
        Instant recordedAt
) {}
