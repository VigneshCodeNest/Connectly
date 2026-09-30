package com.connectly.controller;

import com.connectly.dto.request.UpdateAvatarRequest;
import com.connectly.dto.request.UpdateBioRequest;
import com.connectly.dto.request.UpdateProfileRequest;
import com.connectly.dto.request.UpdateUsernameRequest;
import com.connectly.dto.response.ApiResponse;
import com.connectly.dto.response.UserProfileResponse;
import com.connectly.dto.response.UserSearchResponse;
import com.connectly.entity.User;
import com.connectly.service.AuthService;
import com.connectly.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved successfully", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.updateProfile(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", response));
    }

    @PutMapping("/me/username")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.updateUsername(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Username updated successfully", response));
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.updateAvatar(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Avatar updated successfully", response));
    }

    @PutMapping("/me/bio")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateBio(@Valid @RequestBody UpdateBioRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.updateBio(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("About information updated successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserSearchResponse>> searchUserByEmail(@RequestParam("email") String email) {
        User currentUser = authService.getAuthenticatedUser();
        UserSearchResponse response = userService.searchUserByEmail(email, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("User found", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable("id") Long id) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.getUserById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved", response));
    }

    // Phase 12: Change Password & Settings Endpoints
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody com.connectly.dto.request.ChangePasswordRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        userService.changePassword(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePasswordAlias(@Valid @RequestBody com.connectly.dto.request.ChangePasswordRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        userService.changePassword(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PutMapping("/me/privacy")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updatePrivacySettings(@Valid @RequestBody com.connectly.dto.request.UpdatePrivacySettingsRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.updatePrivacySettings(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Privacy settings updated successfully", response));
    }

    @PutMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateSettings(@Valid @RequestBody com.connectly.dto.request.UpdateSettingsRequest request) {
        User currentUser = authService.getAuthenticatedUser();
        UserProfileResponse response = userService.updateSettings(request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Settings updated successfully", response));
    }
}
