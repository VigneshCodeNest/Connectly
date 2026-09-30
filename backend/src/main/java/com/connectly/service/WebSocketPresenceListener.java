package com.connectly.service;

import com.connectly.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketPresenceListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketPresenceListener.class);

    private final PresenceService presenceService;

    public WebSocketPresenceListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();

        Long userId = extractUserId(principal);
        if (userId != null) {
            logger.debug("WebSocket session connected: userId={}, sessionId={}", userId, sessionId);
            presenceService.handleUserConnected(userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();

        Long userId = extractUserId(principal);
        if (userId != null) {
            logger.debug("WebSocket session disconnected: userId={}, sessionId={}", userId, sessionId);
            presenceService.handleUserDisconnected(userId, sessionId);
        }
    }

    private Long extractUserId(Principal principal) {
        if (principal == null) return null;

        if (principal instanceof Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof UserPrincipal up) {
                return up.getId();
            }
        } else if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        return null;
    }
}
