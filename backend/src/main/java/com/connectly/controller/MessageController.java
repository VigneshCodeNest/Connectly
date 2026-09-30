package com.connectly.controller;

import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final ChatService chatService;
    private final AuthService authService;

    public MessageController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @GetMapping("/starred")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getStarredMessages() {
        User currentUser = authService.getAuthenticatedUser();
        List<MessageResponse> response = chatService.getStarredMessages(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Starred messages retrieved", response));
    }
}
