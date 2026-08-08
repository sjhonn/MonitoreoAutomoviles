package com.autotrack.controller;

import com.autotrack.dto.LocationRequest;
import com.autotrack.dto.LocationResponse;
import com.autotrack.service.TrackingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/vehicles/{vehicleId}/locations")
    public ResponseEntity<LocationResponse> register(
            @PathVariable Long vehicleId,
            @Valid @RequestBody LocationRequest request) {
        LocationResponse created = trackingService.registerLocation(vehicleId, request);
        return ResponseEntity.created(URI.create(
                "/api/tracking/vehicles/" + vehicleId + "/locations/" + created.id())).body(created);
    }

    @GetMapping("/vehicles/{vehicleId}/locations")
    public List<LocationResponse> history(@PathVariable Long vehicleId) {
        return trackingService.history(vehicleId);
    }
}
