package com.connectly.controller;

import com.connectly.dto.request.SendConnectionRequest;
import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.ConnectionResponse;
import com.connectly.dto.response.ConnectionStatusResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final AuthService authService;

    public ConnectionController(ConnectionService connectionService, AuthService authService) {
        this.connectionService = connectionService;
        this.authService = authService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<ConnectionResponse>> sendRequest(@Valid @RequestBody SendConnectionRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        ConnectionResponse response = connectionService.sendRequest(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Connection request sent successfully", response));
    }

    @PostMapping("/request/email")
    public ResponseEntity<ApiResponse<ConnectionResponse>> sendRequestByEmail(@RequestParam("email") String email) {
        User currentUser = authService.getAuthenticatedUser();
        ConnectionResponse response = connectionService.sendRequestByEmail(email, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Connection request sent successfully", response));
    }

    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> getIncomingRequests() {
        User currentUser = authService.getAuthenticatedUser();
        List<ConnectionResponse> response = connectionService.getIncomingRequests(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Incoming connection requests retrieved", response));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> getOutgoingRequests() {
        User currentUser = authService.getAuthenticatedUser();
        List<ConnectionResponse> response = connectionService.getOutgoingRequests(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Outgoing connection requests retrieved", response));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<ConnectionResponse>> acceptRequest(@PathVariable("id") Long requestId) {
        User currentUser = authService.getAuthenticatedUser();
        ConnectionResponse response = connectionService.acceptRequest(requestId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Connection request accepted", response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ConnectionResponse>> rejectRequest(@PathVariable("id") Long requestId) {
        User currentUser = authService.getAuthenticatedUser();
        ConnectionResponse response = connectionService.rejectRequest(requestId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Connection request rejected", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> getConnectedFriends() {
        User currentUser = authService.getAuthenticatedUser();
        List<ConnectionResponse> response = connectionService.getConnectedFriends(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Connected friends retrieved", response));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<ConnectionStatusResponse>> getConnectionStatus(@PathVariable("userId") Long userId) {
        User currentUser = authService.getAuthenticatedUser();
        ConnectionStatusResponse response = connectionService.getConnectionStatus(userId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Connection status retrieved", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeConnection(@PathVariable("id") Long requestId) {
        User currentUser = authService.getAuthenticatedUser();
        connectionService.removeConnection(requestId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Connection removed successfully", null));
    }
}
