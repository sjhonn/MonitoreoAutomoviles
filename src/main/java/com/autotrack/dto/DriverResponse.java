package com.autotrack.dto;

public record DriverResponse(
        Long id,
        String fullName,
        String licenseNumber,
        String phone,
        boolean active
) {}
