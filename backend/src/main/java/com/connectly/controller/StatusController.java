package com.connectly.controller;

import com.connectly.dto.request.CreateStatusRequest;
import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.StatusResponse;
import com.connectly.dto.response.StatusViewResponse;
import com.connectly.dto.response.UserStatusFeedResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.StatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statuses")
public class StatusController {

    private final StatusService statusService;
    private final AuthService authService;

    public StatusController(StatusService statusService, AuthService authService) {
        this.statusService = statusService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StatusResponse>> createStatus(@Valid @RequestBody CreateStatusRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        StatusResponse response = statusService.createStatus(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Status created successfully", response));
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<UserStatusFeedResponse>>> getStatusFeed() {
        User currentUser = authService.getAuthenticatedUser();
        List<UserStatusFeedResponse> feed = statusService.getStatusFeed(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Status feed retrieved", feed));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<StatusResponse>>> getMyStatuses() {
        User currentUser = authService.getAuthenticatedUser();
        List<StatusResponse> response = statusService.getMyStatuses(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("My statuses retrieved", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StatusResponse>> getStatusById(@PathVariable("id") Long statusId) {
        User currentUser = authService.getAuthenticatedUser();
        StatusResponse response = statusService.getStatusById(statusId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Status retrieved", response));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<ApiResponse<StatusResponse>> viewStatus(@PathVariable("id") Long statusId) {
        User currentUser = authService.getAuthenticatedUser();
        StatusResponse response = statusService.viewStatus(statusId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Status viewed", response));
    }

    @GetMapping("/{id}/viewers")
    public ResponseEntity<ApiResponse<List<StatusViewResponse>>> getStatusViewers(@PathVariable("id") Long statusId) {
        User currentUser = authService.getAuthenticatedUser();
        List<StatusViewResponse> viewers = statusService.getStatusViewers(statusId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Status viewers retrieved", viewers));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStatus(@PathVariable("id") Long statusId) {
        User currentUser = authService.getAuthenticatedUser();
        statusService.deleteStatus(statusId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Status deleted successfully", null));
    }
}
