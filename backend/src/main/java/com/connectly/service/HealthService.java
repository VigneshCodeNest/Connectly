package com.connectly.service;

import com.connectly.dto.response.HealthCheckResponse;
import com.connectly.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthService {

    private final UserRepository userRepository;

    public HealthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public HealthCheckResponse getHealthStatus() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("database", "CONNECTED");
        details.put("userCount", userRepository.count());
        details.put("javaVersion", System.getProperty("java.version"));
        details.put("os", System.getProperty("os.name"));

        return new HealthCheckResponse(
            "UP",
            "Connectly Backend API",
            "1.0.0",
            "Phase 1: Foundation & Database Architecture",
            details
        );
    }
}
