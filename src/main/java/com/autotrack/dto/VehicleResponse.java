package com.autotrack.dto;

import com.autotrack.entity.VehicleStatus;
import java.time.Instant;

public record VehicleResponse(
        Long id,
        String plate,
        String brand,
        String model,
        Integer year,
        VehicleStatus status,
        Double maxSpeed,
        DriverResponse driver,
        Double lastLatitude,
        Double lastLongitude,
        Double lastSpeed,
        Double lastHeading,
        Instant lastSeen
) {}
