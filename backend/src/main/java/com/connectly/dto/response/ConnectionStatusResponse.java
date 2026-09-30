package com.connectly.dto.response;

public class ConnectionStatusResponse {
    private Long userId;
    private String status; // "NONE", "REQUEST_SENT", "REQUEST_RECEIVED", "CONNECTED", "REJECTED"
    private Long requestId;

    public ConnectionStatusResponse() {}

    public ConnectionStatusResponse(Long userId, String status, Long requestId) {
        this.userId = userId;
        this.status = status;
        this.requestId = requestId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
}
