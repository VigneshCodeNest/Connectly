package com.connectly.controller;

import com.connectly.dto.request.AddGroupMembersRequest;
import com.connectly.dto.request.CreateGroupRequest;
import com.connectly.dto.request.UpdateGroupRequest;
import com.connectly.dto.request.UpdateMemberRoleRequest;
import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.GroupMemberResponse;
import com.connectly.dto.response.GroupResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;
    private final AuthService authService;

    public GroupController(GroupService groupService, AuthService authService) {
        this.groupService = groupService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        GroupResponse response = groupService.createGroup(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getUserGroups() {
        User currentUser = authService.getAuthenticatedUser();
        List<GroupResponse> response = groupService.getUserGroups(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("User groups retrieved", response));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupDetails(@PathVariable("groupId") Long groupId) {
        User currentUser = authService.getAuthenticatedUser();
        GroupResponse response = groupService.getGroupDetails(groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Group details retrieved", response));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroupInfo(
            @PathVariable("groupId") Long groupId,
            @Valid @RequestBody UpdateGroupRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();
        GroupResponse response = groupService.updateGroupInfo(groupId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Group updated successfully", response));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(@PathVariable("groupId") Long groupId) {
        User currentUser = authService.getAuthenticatedUser();
        List<GroupMemberResponse> response = groupService.getGroupMembers(groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Group members retrieved", response));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> addMembers(
            @PathVariable("groupId") Long groupId,
            @Valid @RequestBody AddGroupMembersRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();
        List<GroupMemberResponse> response = groupService.addMembers(groupId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Members added successfully", response));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable("groupId") Long groupId,
            @PathVariable("userId") Long userId
    ) {
        User currentUser = authService.getAuthenticatedUser();
        groupService.removeMember(groupId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Member removed successfully", null));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(@PathVariable("groupId") Long groupId) {
        User currentUser = authService.getAuthenticatedUser();
        groupService.leaveGroup(groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Left group successfully", null));
    }

    @PutMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> changeMemberRole(
            @PathVariable("groupId") Long groupId,
            @PathVariable("userId") Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        User currentUser = authService.getAuthenticatedUser();
        groupService.changeMemberRole(groupId, userId, request.getRole(), currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Member role updated successfully", null));
    }
}
