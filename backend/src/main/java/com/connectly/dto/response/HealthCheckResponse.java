package com.connectly.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public class HealthCheckResponse {
    private String status;
    private String application;
    private String version;
    private String activePhase;
    private Map<String, Object> details;
    private LocalDateTime timestamp;

    public HealthCheckResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public HealthCheckResponse(String status, String application, String version, String activePhase, Map<String, Object> details) {
        this.status = status;
        this.application = application;
        this.version = version;
        this.activePhase = activePhase;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApplication() { return application; }
    public void setApplication(String application) { this.application = application; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getActivePhase() { return activePhase; }
    public void setActivePhase(String activePhase) { this.activePhase = activePhase; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
