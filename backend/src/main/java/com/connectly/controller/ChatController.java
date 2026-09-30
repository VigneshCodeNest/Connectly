package com.connectly.controller;

import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.ForwardMessageRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.ChatResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.response.UserSummaryResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    public ChatController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ChatResponse>> createOrGetDirectChat(@Valid @RequestBody CreateDirectChatRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        ChatResponse response = chatService.getOrCreateDirectChat(request, currentUser);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.ok("Direct chat loaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatResponse>>> getUserChats() {
        User currentUser = authService.getAuthenticatedUser();
        List<ChatResponse> response = chatService.getUserChats(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("User chats retrieved", response));
    }

    @GetMapping("/starred")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getStarredMessages() {
        User currentUser = authService.getAuthenticatedUser();
        List<MessageResponse> response = chatService.getStarredMessages(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Starred messages retrieved", response));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ApiResponse<ChatResponse>> getChatDetails(@PathVariable("chatId") Long chatId) {
        User currentUser = authService.getAuthenticatedUser();
        ChatResponse response = chatService.getChatDetails(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat details retrieved", response));
    }

    @GetMapping("/{chatId}/participants")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getChatParticipants(@PathVariable("chatId") Long chatId) {
        User currentUser = authService.getAuthenticatedUser();
        List<UserSummaryResponse> response = chatService.getChatParticipants(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Participants retrieved", response));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getChatMessages(
            @PathVariable("chatId") Long chatId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        User currentUser = authService.getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> response = chatService.getChatMessages(chatId, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat messages retrieved", response));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable("chatId") Long chatId,
            @RequestBody SendMessageRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();
        MessageResponse response = chatService.sendMessage(chatId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message sent successfully", response));
    }

    @PostMapping("/{chatId}/messages/{messageId}/forward")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> forwardMessage(
            @PathVariable("chatId") Long chatId,
            @PathVariable("messageId") Long messageId,
            @RequestBody ForwardMessageRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();
        List<MessageResponse> response = chatService.forwardMessage(chatId, messageId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message forwarded successfully", response));
    }

    @DeleteMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable("chatId") Long chatId,
            @PathVariable("messageId") Long messageId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        chatService.deleteMessage(chatId, messageId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Message deleted successfully", null));
    }

    @PostMapping("/{chatId}/messages/{messageId}/star")
    public ResponseEntity<ApiResponse<Void>> starMessage(
            @PathVariable("chatId") Long chatId,
            @PathVariable("messageId") Long messageId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        chatService.starMessage(chatId, messageId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Message starred successfully", null));
    }

    @DeleteMapping("/{chatId}/messages/{messageId}/star")
    public ResponseEntity<ApiResponse<Void>> unstarMessage(
            @PathVariable("chatId") Long chatId,
            @PathVariable("messageId") Long messageId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        chatService.unstarMessage(chatId, messageId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Message unstarred successfully", null));
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<ApiResponse<Void>> markChatAsRead(@PathVariable("chatId") Long chatId) {
        User currentUser = authService.getAuthenticatedUser();
        chatService.markChatMessagesAsRead(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat messages marked as read", null));
    }

    @PostMapping("/{chatId}/messages/{messageId}/delivered")
    public ResponseEntity<ApiResponse<Void>> markMessageAsDelivered(
            @PathVariable("chatId") Long chatId,
            @PathVariable("messageId") Long messageId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        chatService.markMessageAsDelivered(chatId, messageId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Message marked as delivered", null));
    }

    @PostMapping("/{chatId}/delivered")
    public ResponseEntity<ApiResponse<Void>> markAllAsDelivered(@PathVariable("chatId") Long chatId) {
        User currentUser = authService.getAuthenticatedUser();
        chatService.markAllChatMessagesAsDelivered(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Messages marked as delivered", null));
    }

    // Phase 11: Chat Preferences & Muting Endpoints
    @PostMapping("/{chatId}/mute")
    public ResponseEntity<ApiResponse<com.connectly.dto.response.ChatPreferencesResponse>> muteChat(
            @PathVariable("chatId") Long chatId,
            @RequestBody(required = false) com.connectly.dto.request.MuteChatRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();
        com.connectly.dto.response.ChatPreferencesResponse response = chatService.muteChat(chatId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat muted successfully", response));
    }

    @PostMapping("/{chatId}/unmute")
    public ResponseEntity<ApiResponse<com.connectly.dto.response.ChatPreferencesResponse>> unmuteChat(
            @PathVariable("chatId") Long chatId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        com.connectly.dto.response.ChatPreferencesResponse response = chatService.unmuteChat(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat unmuted successfully", response));
    }

    @DeleteMapping("/{chatId}/mute")
    public ResponseEntity<ApiResponse<com.connectly.dto.response.ChatPreferencesResponse>> deleteMute(
            @PathVariable("chatId") Long chatId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        com.connectly.dto.response.ChatPreferencesResponse response = chatService.unmuteChat(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat unmuted successfully", response));
    }

    @GetMapping("/{chatId}/preferences")
    public ResponseEntity<ApiResponse<com.connectly.dto.response.ChatPreferencesResponse>> getChatPreferences(
            @PathVariable("chatId") Long chatId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        com.connectly.dto.response.ChatPreferencesResponse response = chatService.getChatPreferences(chatId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Chat preferences retrieved", response));
    }
}
