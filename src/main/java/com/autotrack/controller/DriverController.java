package com.autotrack.controller;

import com.autotrack.dto.DriverRequest;
import com.autotrack.dto.DriverResponse;
import com.autotrack.service.DriverService;
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
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<DriverResponse> list(@RequestParam(required = false) String q) {
        return driverService.list(q);
    }

    @GetMapping("/{id}")
    public DriverResponse get(@PathVariable Long id) {
        return driverService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest request) {
        DriverResponse created = driverService.create(request);
        return ResponseEntity.created(URI.create("/api/drivers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DriverResponse update(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        return driverService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
