package com.autotrack.controller;

import com.autotrack.dto.VehicleRequest;
import com.autotrack.dto.VehicleResponse;
import com.autotrack.service.VehicleService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<VehicleResponse> list(@RequestParam(required = false) String q) {
        return vehicleService.list(q);
    }

    @GetMapping("/{id}")
    public VehicleResponse get(@PathVariable Long id) {
        return vehicleService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.create(request);
        return ResponseEntity.created(URI.create("/api/vehicles/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
