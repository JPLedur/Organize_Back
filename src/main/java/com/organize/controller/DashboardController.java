package com.organize.controller;

import com.organize.dto.DashboardDTO;
import com.organize.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Dashboard geral
    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboardData() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

    // Dashboard do cliente
    @GetMapping("/{clientId}")
    public ResponseEntity<DashboardDTO> getDashboardClientData(@PathVariable UUID clientId) {
        return ResponseEntity.ok(dashboardService.getDashboardDataForClient(clientId));
    }
}
