package com.autotrack.dto;

public record DashboardResponse(
        long totalVehicles,
        long onlineVehicles,
        long movingVehicles,
        long stoppedVehicles,
        long maintenanceVehicles,
        long outOfServiceVehicles,
        long activeAlerts
) {}
