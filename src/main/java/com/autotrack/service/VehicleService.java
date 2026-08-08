package com.autotrack.service;

import com.autotrack.dto.VehicleRequest;
import com.autotrack.dto.VehicleResponse;
import com.autotrack.entity.Driver;
import com.autotrack.entity.Vehicle;
import com.autotrack.entity.VehicleStatus;
import com.autotrack.exception.NotFoundException;
import com.autotrack.repository.AlertRepository;
import com.autotrack.repository.VehicleLocationRepository;
import com.autotrack.repository.VehicleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleLocationRepository locationRepository;
    private final AlertRepository alertRepository;
    private final DriverService driverService;

    public VehicleService(
            VehicleRepository vehicleRepository,
            VehicleLocationRepository locationRepository,
            AlertRepository alertRepository,
            DriverService driverService) {
        this.vehicleRepository = vehicleRepository;
        this.locationRepository = locationRepository;
        this.alertRepository = alertRepository;
        this.driverService = driverService;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> list(String query) {
        List<Vehicle> vehicles;
        if (query == null || query.isBlank()) {
            vehicles = vehicleRepository.findAllByOrderByPlateAsc();
        } else {
            String q = query.trim();
            vehicles = vehicleRepository
                    .findByPlateContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrderByPlateAsc(q, q, q);
        }
        return vehicles.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        String plate = normalizePlate(request.plate());
        if (vehicleRepository.existsByPlateIgnoreCase(plate)) {
            throw new IllegalArgumentException("A vehicle with that plate already exists.");
        }
        Vehicle vehicle = new Vehicle();
        apply(vehicle, request, plate);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = findEntity(id);
        String plate = normalizePlate(request.plate());
        if (!vehicle.getPlate().equalsIgnoreCase(plate) && vehicleRepository.existsByPlateIgnoreCase(plate)) {
            throw new IllegalArgumentException("A vehicle with that plate already exists.");
        }
        apply(vehicle, request, plate);
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findEntity(id);
        alertRepository.deleteByVehicleId(id);
        locationRepository.deleteByVehicleId(id);
        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle findEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + id));
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getStatus(),
                vehicle.getMaxSpeed(),
                driverService.toResponse(vehicle.getDriver()),
                vehicle.getLastLatitude(),
                vehicle.getLastLongitude(),
                vehicle.getLastSpeed(),
                vehicle.getLastHeading(),
                vehicle.getLastSeen());
    }

    private void apply(Vehicle vehicle, VehicleRequest request, String plate) {
        vehicle.setPlate(plate);
        vehicle.setBrand(request.brand().trim());
        vehicle.setModel(request.model().trim());
        vehicle.setYear(request.year());
        vehicle.setStatus(request.status() == null ? VehicleStatus.AVAILABLE : request.status());
        vehicle.setMaxSpeed(request.maxSpeed() == null ? 80.0 : request.maxSpeed());
        Driver driver = request.driverId() == null ? null : driverService.findEntity(request.driverId());
        vehicle.setDriver(driver);
    }

    private String normalizePlate(String value) {
        return value.trim().toUpperCase();
    }
}
