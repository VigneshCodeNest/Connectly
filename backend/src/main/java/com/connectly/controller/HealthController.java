package com.connectly.controller;

import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.HealthCheckResponse;
import com.connectly.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthCheckResponse>> checkHealth() {
        HealthCheckResponse response = healthService.getHealthStatus();
        return ResponseEntity.ok(ApiResponse.ok("Connectly backend service is healthy", response));
    }
}
