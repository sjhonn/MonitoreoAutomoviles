package com.autotrack.repository;

import com.autotrack.entity.Driver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);
    Optional<Driver> findByLicenseNumberIgnoreCase(String licenseNumber);
    List<Driver> findByFullNameContainingIgnoreCaseOrLicenseNumberContainingIgnoreCaseOrderByFullNameAsc(String fullName, String licenseNumber);
}
