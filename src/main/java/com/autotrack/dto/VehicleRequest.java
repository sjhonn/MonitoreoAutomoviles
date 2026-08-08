package com.autotrack.dto;

import com.autotrack.entity.VehicleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank @Size(max = 20) String plate,
        @NotBlank @Size(max = 80) String brand,
        @NotBlank @Size(max = 80) String model,
        @NotNull @Min(1950) @Max(2100) Integer year,
        VehicleStatus status,
        @Positive Double maxSpeed,
        Long driverId
) {}
