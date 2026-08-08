package com.autotrack.controller;

import com.autotrack.dto.AlertResponse;
import com.autotrack.service.AlertService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> list() {
        return alertService.list();
    }

    @PatchMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('ADMIN')")
    public AlertResponse acknowledge(@PathVariable Long id) {
        return alertService.acknowledge(id);
    }
}
