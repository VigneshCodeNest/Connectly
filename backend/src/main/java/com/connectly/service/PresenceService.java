package com.connectly.service;

import com.connectly.dto.websocket.WsPresencePayload;

public interface PresenceService {
    void handleUserConnected(Long userId, String sessionId);
    void handleUserDisconnected(Long userId, String sessionId);
    boolean isUserOnline(Long userId);
    WsPresencePayload getUserPresence(Long userId);
}
