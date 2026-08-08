package com.autotrack.repository;

import com.autotrack.entity.VehicleLocation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {
    List<VehicleLocation> findTop100ByVehicleIdOrderByRecordedAtDesc(Long vehicleId);
    void deleteByVehicleId(Long vehicleId);
}
