package com.autotrack.service;

import com.autotrack.dto.AlertResponse;
import com.autotrack.entity.Alert;
import com.autotrack.exception.NotFoundException;
import com.autotrack.repository.AlertRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {
    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> list() {
        return alertRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlertResponse acknowledge(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + id));
        alert.setAcknowledged(true);
        return toResponse(alertRepository.save(alert));
    }

    public AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getVehicle().getId(),
                alert.getVehicle().getPlate(),
                alert.getType(),
                alert.getMessage(),
                alert.isAcknowledged(),
                alert.getCreatedAt());
    }
}
