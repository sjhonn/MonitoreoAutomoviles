package com.autotrack.service;

import com.autotrack.dto.DriverRequest;
import com.autotrack.dto.DriverResponse;
import com.autotrack.entity.Driver;
import com.autotrack.exception.NotFoundException;
import com.autotrack.repository.DriverRepository;
import com.autotrack.repository.VehicleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverService(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> list(String query) {
        List<Driver> drivers;
        if (query == null || query.isBlank()) {
            drivers = driverRepository.findAll();
        } else {
            String q = query.trim();
            drivers = driverRepository
                    .findByFullNameContainingIgnoreCaseOrLicenseNumberContainingIgnoreCaseOrderByFullNameAsc(q, q);
        }
        return drivers.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse get(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public DriverResponse create(DriverRequest request) {
        String license = normalizeLicense(request.licenseNumber());
        if (driverRepository.existsByLicenseNumberIgnoreCase(license)) {
            throw new IllegalArgumentException("A driver with that license already exists.");
        }
        Driver driver = new Driver();
        apply(driver, request, license);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse update(Long id, DriverRequest request) {
        Driver driver = findEntity(id);
        String license = normalizeLicense(request.licenseNumber());
        driverRepository.findByLicenseNumberIgnoreCase(license)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A driver with that license already exists.");
                });
        apply(driver, request, license);
        return toResponse(driverRepository.save(driver));
    }

    @Transactional
    public void delete(Long id) {
        Driver driver = findEntity(id);
        if (vehicleRepository.existsByDriverId(id)) {
            throw new IllegalStateException("The driver is assigned to a vehicle and cannot be deleted.");
        }
        driverRepository.delete(driver);
    }

    @Transactional(readOnly = true)
    public Driver findEntity(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Driver not found: " + id));
    }

    public DriverResponse toResponse(Driver driver) {
        if (driver == null) {
            return null;
        }
        return new DriverResponse(
                driver.getId(),
                driver.getFullName(),
                driver.getLicenseNumber(),
                driver.getPhone(),
                driver.isActive());
    }

    private void apply(Driver driver, DriverRequest request, String license) {
        driver.setFullName(request.fullName().trim());
        driver.setLicenseNumber(license);
        driver.setPhone(request.phone() == null ? null : request.phone().trim());
        driver.setActive(request.active() == null || request.active());
    }

    private String normalizeLicense(String value) {
        return value.trim().toUpperCase();
    }
}
