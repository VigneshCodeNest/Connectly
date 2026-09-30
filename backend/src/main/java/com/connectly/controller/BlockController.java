package com.connectly.controller;

import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.BlockedUserResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.BlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class BlockController {

    private final BlockService blockService;
    private final AuthService authService;

    public BlockController(BlockService blockService, AuthService authService) {
        this.blockService = blockService;
        this.authService = authService;
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<BlockedUserResponse>> blockUser(
            @PathVariable Long userId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        BlockedUserResponse response = blockService.blockUser(userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User blocked successfully", response));
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<ApiResponse<BlockedUserResponse>> blockUserAlias(
            @PathVariable Long userId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        BlockedUserResponse response = blockService.blockUser(userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User blocked successfully", response));
    }

    @DeleteMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @PathVariable Long userId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        blockService.unblockUser(userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User unblocked successfully", null));
    }

    @PostMapping("/unblock/{userId}")
    public ResponseEntity<ApiResponse<Void>> unblockUserAlias(
            @PathVariable Long userId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        blockService.unblockUser(userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User unblocked successfully", null));
    }

    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse<List<BlockedUserResponse>>> getBlockedUsers() {
        User currentUser = authService.getAuthenticatedUser();
        List<BlockedUserResponse> list = blockService.getBlockedUsers(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Blocked users retrieved successfully", list));
    }

    @GetMapping("/{userId}/block-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBlockStatus(
            @PathVariable Long userId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        boolean blockedByMe = blockService.isBlockedBy(currentUser.getId(), userId);
        boolean blockedByThem = blockService.isBlockedBy(userId, currentUser.getId());
        boolean isBlocked = blockedByMe || blockedByThem;

        Map<String, Object> map = new HashMap<>();
        map.put("targetUserId", userId);
        map.put("isBlocked", isBlocked);
        map.put("blockedByMe", blockedByMe);
        map.put("blockedByThem", blockedByThem);

        return ResponseEntity.ok(ApiResponse.success("Block status retrieved", map));
    }
}
