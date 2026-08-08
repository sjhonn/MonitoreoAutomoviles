package com.autotrack.service;

import com.autotrack.dto.LocationRequest;
import com.autotrack.entity.Vehicle;
import com.autotrack.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class SimulatorService {
    private static final double[][] ROUTE = {
            {-12.046374, -77.042793},
            {-12.046050, -77.042250},
            {-12.045700, -77.041720},
            {-12.045300, -77.041180},
            {-12.044880, -77.040650},
            {-12.044460, -77.040120},
            {-12.044090, -77.039620},
            {-12.043740, -77.039120},
            {-12.043420, -77.038610},
            {-12.043150, -77.038070},
            {-12.043420, -77.038610},
            {-12.043740, -77.039120},
            {-12.044090, -77.039620},
            {-12.044460, -77.040120},
            {-12.044880, -77.040650},
            {-12.045300, -77.041180},
            {-12.045700, -77.041720},
            {-12.046050, -77.042250}
    };

    private final VehicleRepository vehicleRepository;
    private final TrackingService trackingService;
    private final AtomicInteger cursor = new AtomicInteger();

    public SimulatorService(VehicleRepository vehicleRepository, TrackingService trackingService) {
        this.vehicleRepository = vehicleRepository;
        this.trackingService = trackingService;
    }

    @Scheduled(fixedRateString = "${app.simulator.interval-ms:2000}", initialDelayString = "${app.simulator.initial-delay-ms:3000}")
    public void tick() {
        List<Vehicle> vehicles = vehicleRepository.findAllByOrderByPlateAsc();
        if (vehicles.isEmpty()) {
            return;
        }

        Vehicle vehicle = vehicles.get(0);
        int index = Math.floorMod(cursor.getAndIncrement(), ROUTE.length);
        double[] point = ROUTE[index];
        double speed = index % 7 == 0 ? 0.0 : 42.0 + (index % 6) * 10.5;
        double heading = index < ROUTE.length / 2 ? 135.0 : 315.0;

        trackingService.registerLocation(
                vehicle.getId(),
                new LocationRequest(point[0], point[1], speed, heading, Instant.now()));
    }
}
