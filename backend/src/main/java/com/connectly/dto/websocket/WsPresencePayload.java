package com.connectly.dto.websocket;

import java.time.LocalDateTime;

public class WsPresencePayload {
    private Long userId;
    private boolean isOnline;
    private LocalDateTime lastSeen;

    public WsPresencePayload() {}

    public WsPresencePayload(Long userId, boolean isOnline, LocalDateTime lastSeen) {
        this.userId = userId;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
}
