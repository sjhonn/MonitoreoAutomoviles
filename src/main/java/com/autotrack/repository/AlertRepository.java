package com.autotrack.repository;

import com.autotrack.entity.Alert;
import com.autotrack.entity.AlertType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findTop100ByOrderByCreatedAtDesc();
    long countByAcknowledgedFalse();
    boolean existsByVehicleIdAndTypeAndAcknowledgedFalseAndCreatedAtAfter(Long vehicleId, AlertType type, Instant createdAt);
    void deleteByVehicleId(Long vehicleId);
}
