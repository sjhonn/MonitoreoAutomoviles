package com.autotrack.repository;

import com.autotrack.entity.Vehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlateIgnoreCase(String plate);
    boolean existsByDriverId(Long driverId);
    List<Vehicle> findByPlateContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrderByPlateAsc(
            String plate, String brand, String model);
    List<Vehicle> findAllByOrderByPlateAsc();
}
