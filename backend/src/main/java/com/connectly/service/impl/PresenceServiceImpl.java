package com.connectly.service.impl;

import com.connectly.dto.websocket.WsEvent;
import com.connectly.dto.websocket.WsPresencePayload;
import com.connectly.entity.User;
import com.connectly.repository.UserRepository;
import com.connectly.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceServiceImpl implements PresenceService {

    private static final Logger logger = LoggerFactory.getLogger(PresenceServiceImpl.class);

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Thread-safe map tracking active session IDs for each userId
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public PresenceServiceImpl(UserRepository userRepository, @Lazy SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public void handleUserConnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) return;

        userSessions.compute(userId, (key, sessions) -> {
            if (sessions == null) {
                sessions = ConcurrentHashMap.newKeySet();
            }
            sessions.add(sessionId);
            return sessions;
        });

        // If this user was not marked online, update database and broadcast
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && !user.isOnline()) {
            user.setOnline(true);
            userRepository.save(user);
            logger.info("User {} is now ONLINE (active sessions: {})", userId, userSessions.get(userId).size());

            WsPresencePayload payload = new WsPresencePayload(userId, true, null);
            messagingTemplate.convertAndSend("/topic/presence", new WsEvent<>("USER_PRESENCE", payload));
        }
    }

    @Override
    @Transactional
    public void handleUserDisconnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) return;

        boolean isOfflineNow = false;

        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                isOfflineNow = true;
            }
        } else {
            isOfflineNow = true;
        }

        if (isOfflineNow) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setOnline(false);
                userRepository.save(user);
                logger.info("User {} is now OFFLINE", userId);

                WsPresencePayload payload = new WsPresencePayload(userId, false, LocalDateTime.now());
                messagingTemplate.convertAndSend("/topic/presence", new WsEvent<>("USER_PRESENCE", payload));
            }
        }
    }

    @Override
    public boolean isUserOnline(Long userId) {
        if (userId == null) return false;
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public WsPresencePayload getUserPresence(Long userId) {
        boolean online = isUserOnline(userId);
        User user = userRepository.findById(userId).orElse(null);
        LocalDateTime lastSeen = (user != null && !online) ? user.getUpdatedAt() : null;
        return new WsPresencePayload(userId, online, lastSeen);
    }
}
