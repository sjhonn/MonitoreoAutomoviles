package com.autotrack.service;

import com.autotrack.dto.DashboardResponse;
import com.autotrack.entity.Vehicle;
import com.autotrack.entity.VehicleStatus;
import com.autotrack.repository.AlertRepository;
import com.autotrack.repository.VehicleRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final VehicleRepository vehicleRepository;
    private final AlertRepository alertRepository;

    public DashboardService(VehicleRepository vehicleRepository, AlertRepository alertRepository) {
        this.vehicleRepository = vehicleRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse metrics() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        Instant onlineThreshold = Instant.now().minus(Duration.ofSeconds(30));

        long online = vehicles.stream()
                .filter(v -> v.getLastSeen() != null && v.getLastSeen().isAfter(onlineThreshold))
                .count();
        long moving = count(vehicles, VehicleStatus.MOVING);
        long stopped = count(vehicles, VehicleStatus.STOPPED);
        long maintenance = count(vehicles, VehicleStatus.MAINTENANCE);
        long outOfService = count(vehicles, VehicleStatus.OUT_OF_SERVICE);

        return new DashboardResponse(
                vehicles.size(),
                online,
                moving,
                stopped,
                maintenance,
                outOfService,
                alertRepository.countByAcknowledgedFalse());
    }

    private long count(List<Vehicle> vehicles, VehicleStatus status) {
        return vehicles.stream().filter(v -> v.getStatus() == status).count();
    }
}
