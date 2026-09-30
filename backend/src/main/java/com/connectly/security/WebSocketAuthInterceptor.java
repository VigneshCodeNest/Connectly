package com.connectly.security;

import com.connectly.repository.ChatParticipantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private static final Pattern CHAT_TOPIC_PATTERN = Pattern.compile("^/(?:topic|queue)/chat[./](\\d+)$");

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ChatParticipantRepository chatParticipantRepository;

    public WebSocketAuthInterceptor(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService,
            ChatParticipantRepository chatParticipantRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            handleSend(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            logger.warn("WebSocket CONNECT rejected: Invalid or missing JWT token");
            throw new AccessDeniedException("Invalid or missing JWT token for WebSocket connection");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserById(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        accessor.setUser(authentication);
        logger.info("WebSocket connection authenticated for user: {} (ID: {})", userDetails.getUsername(), userId);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            logger.warn("WebSocket SUBSCRIBE rejected: Unauthenticated user");
            throw new AccessDeniedException("Authentication required to subscribe to destinations");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        // Enforce destination authorization for chat topics (e.g. /topic/chat.123 or /topic/chat/123)
        Matcher matcher = CHAT_TOPIC_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long chatId = Long.parseLong(matcher.group(1));
            boolean isParticipant = chatParticipantRepository.existsByChatIdAndUserId(chatId, userPrincipal.getId());
            if (!isParticipant) {
                logger.warn("Subscription denied for user {} (ID: {}) to destination {}",
                        userPrincipal.getUsername(), userPrincipal.getId(), destination);
                throw new AccessDeniedException("Access denied: You are not an authorized participant in conversation " + chatId);
            }
            logger.debug("User {} subscribed to chat topic: {}", userPrincipal.getUsername(), destination);
        }
    }

    private void handleSend(StompHeaderAccessor accessor) {
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            logger.warn("WebSocket SEND rejected: Unauthenticated user");
            throw new AccessDeniedException("Authentication required to send WebSocket messages");
        }
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7).trim();
            }
            return bearerToken != null ? bearerToken.trim() : null;
        }

        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0).trim();
        }

        List<String> authTokenHeaders = accessor.getNativeHeader("Auth-Token");
        if (authTokenHeaders != null && !authTokenHeaders.isEmpty()) {
            return authTokenHeaders.get(0).trim();
        }

        return null;
    }
}
