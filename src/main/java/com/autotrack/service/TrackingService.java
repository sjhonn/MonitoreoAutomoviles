package com.autotrack.service;

import com.autotrack.dto.LocationRequest;
import com.autotrack.dto.LocationResponse;
import com.autotrack.entity.Alert;
import com.autotrack.entity.AlertType;
import com.autotrack.entity.Vehicle;
import com.autotrack.entity.VehicleLocation;
import com.autotrack.entity.VehicleStatus;
import com.autotrack.repository.AlertRepository;
import com.autotrack.repository.VehicleLocationRepository;
import com.autotrack.repository.VehicleRepository;
import com.autotrack.websocket.LocationWebSocketHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackingService {
    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final VehicleLocationRepository locationRepository;
    private final AlertRepository alertRepository;
    private final LocationWebSocketHandler webSocketHandler;

    public TrackingService(
            VehicleService vehicleService,
            VehicleRepository vehicleRepository,
            VehicleLocationRepository locationRepository,
            AlertRepository alertRepository,
            LocationWebSocketHandler webSocketHandler) {
        this.vehicleService = vehicleService;
        this.vehicleRepository = vehicleRepository;
        this.locationRepository = locationRepository;
        this.alertRepository = alertRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @Transactional
    public LocationResponse registerLocation(Long vehicleId, LocationRequest request) {
        Vehicle vehicle = vehicleService.findEntity(vehicleId);
        Instant recordedAt = request.recordedAt() == null ? Instant.now() : request.recordedAt();

        VehicleLocation location = new VehicleLocation();
        location.setVehicle(vehicle);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setSpeed(request.speed());
        location.setHeading(request.heading());
        location.setRecordedAt(recordedAt);
        VehicleLocation saved = locationRepository.save(location);

        vehicle.setLastLatitude(request.latitude());
        vehicle.setLastLongitude(request.longitude());
        vehicle.setLastSpeed(request.speed());
        vehicle.setLastHeading(request.heading());
        vehicle.setLastSeen(recordedAt);
        if (vehicle.getStatus() != VehicleStatus.MAINTENANCE
                && vehicle.getStatus() != VehicleStatus.OUT_OF_SERVICE) {
            vehicle.setStatus(request.speed() > 1.0 ? VehicleStatus.MOVING : VehicleStatus.STOPPED);
        }
        vehicleRepository.save(vehicle);

        createSpeedAlertIfNeeded(vehicle, request.speed());
        LocationResponse response = toResponse(saved);
        webSocketHandler.broadcast(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> history(Long vehicleId) {
        vehicleService.findEntity(vehicleId);
        return locationRepository.findTop100ByVehicleIdOrderByRecordedAtDesc(vehicleId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void createSpeedAlertIfNeeded(Vehicle vehicle, double speed) {
        if (vehicle.getMaxSpeed() == null || speed <= vehicle.getMaxSpeed()) {
            return;
        }
        Instant window = Instant.now().minus(Duration.ofMinutes(2));
        boolean recentAlert = alertRepository
                .existsByVehicleIdAndTypeAndAcknowledgedFalseAndCreatedAtAfter(
                        vehicle.getId(), AlertType.SPEEDING, window);
        if (recentAlert) {
            return;
        }

        Alert alert = new Alert();
        alert.setVehicle(vehicle);
        alert.setType(AlertType.SPEEDING);
        alert.setMessage(String.format(
                "Vehicle %s exceeded its speed limit: %.1f km/h > %.1f km/h.",
                vehicle.getPlate(), speed, vehicle.getMaxSpeed()));
        alertRepository.save(alert);
    }

    private LocationResponse toResponse(VehicleLocation location) {
        return new LocationResponse(
                location.getId(),
                location.getVehicle().getId(),
                location.getVehicle().getPlate(),
                location.getLatitude(),
                location.getLongitude(),
                location.getSpeed(),
                location.getHeading(),
                location.getRecordedAt());
    }
}
