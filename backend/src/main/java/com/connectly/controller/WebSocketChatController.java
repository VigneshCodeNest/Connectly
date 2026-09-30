package com.connectly.controller;

import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.websocket.WsChatMessagePayload;
import com.connectly.dto.websocket.WsEvent;
import com.connectly.dto.websocket.WsStatusUpdatePayload;
import com.connectly.dto.websocket.WsTypingPayload;
import com.connectly.entity.User;
import com.connectly.repository.ChatParticipantRepository;
import com.connectly.repository.UserRepository;
import com.connectly.security.UserPrincipal;
import com.connectly.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketChatController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChatController.class);

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatController(
            ChatService chatService,
            UserRepository userRepository,
            ChatParticipantRepository chatParticipantRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatService = chatService;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload WsChatMessagePayload payload, Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        if (payload.getChatId() == null) {
            throw new IllegalArgumentException("Chat ID is required");
        }

        SendMessageRequest request = new SendMessageRequest();
        request.setContent(payload.getContent());
        request.setMessageType(payload.getMessageType());
        request.setReplyToId(payload.getReplyToId());
        request.setForwarded(payload.isForwarded());
        request.setFileUrl(payload.getFileUrl());
        request.setFileName(payload.getFileName());
        request.setFileType(payload.getFileType());
        request.setFileSize(payload.getFileSize());

        chatService.sendMessage(payload.getChatId(), request, currentUser);
    }

    @MessageMapping("/chat.markDelivered")
    public void markDelivered(@Payload WsStatusUpdatePayload payload, Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        if (payload.getChatId() == null) return;

        if (payload.getMessageId() != null) {
            chatService.markMessageAsDelivered(payload.getChatId(), payload.getMessageId(), currentUser);
        } else {
            chatService.markAllChatMessagesAsDelivered(payload.getChatId(), currentUser);
        }
    }

    @MessageMapping("/chat.markRead")
    public void markRead(@Payload WsStatusUpdatePayload payload, Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        if (payload.getChatId() == null) return;

        chatService.markChatMessagesAsRead(payload.getChatId(), currentUser);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload WsTypingPayload payload, Principal principal) {
        User currentUser = getAuthenticatedUser(principal);
        if (payload.getChatId() == null) return;

        boolean isParticipant = chatParticipantRepository.existsByChatIdAndUserId(payload.getChatId(), currentUser.getId());
        if (!isParticipant) {
            throw new AccessDeniedException("Access denied: You are not a participant in this conversation");
        }

        WsTypingPayload broadcastPayload = new WsTypingPayload(
                payload.getChatId(),
                currentUser.getId(),
                currentUser.getName(),
                payload.isTyping()
        );

        messagingTemplate.convertAndSend("/topic/chat." + payload.getChatId(), new WsEvent<>("TYPING", broadcastPayload));
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("WebSocket principal is not authenticated");
        }

        Long rawUserId = null;
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof UserPrincipal up) {
            rawUserId = up.getId();
        } else if (principal instanceof UserPrincipal up) {
            rawUserId = up.getId();
        }

        if (rawUserId == null) {
            throw new AccessDeniedException("Could not extract user identity from WebSocket session");
        }

        final Long userId = rawUserId;
        return userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found with ID: " + userId));
    }
}
