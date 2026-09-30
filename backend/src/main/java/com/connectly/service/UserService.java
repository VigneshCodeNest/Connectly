package com.connectly.service;

import com.connectly.dto.request.UpdateAvatarRequest;
import com.connectly.dto.request.UpdateBioRequest;
import com.connectly.dto.request.UpdateProfileRequest;
import com.connectly.dto.request.UpdateUsernameRequest;
import com.connectly.dto.response.UserProfileResponse;
import com.connectly.dto.response.UserSearchResponse;
import com.connectly.entity.User;

public interface UserService {
    UserProfileResponse getCurrentUserProfile(User currentUser);
    UserProfileResponse getUserById(Long id, User currentUser);
    UserSearchResponse searchUserByEmail(String email, User currentUser);
    UserProfileResponse updateProfile(UpdateProfileRequest request, User currentUser);
    UserProfileResponse updateUsername(UpdateUsernameRequest request, User currentUser);
    UserProfileResponse updateAvatar(UpdateAvatarRequest request, User currentUser);
    UserProfileResponse updateBio(UpdateBioRequest request, User currentUser);

    // Phase 12: Change Password & Settings
    void changePassword(com.connectly.dto.request.ChangePasswordRequest request, User currentUser);
    UserProfileResponse updatePrivacySettings(com.connectly.dto.request.UpdatePrivacySettingsRequest request, User currentUser);
    UserProfileResponse updateSettings(com.connectly.dto.request.UpdateSettingsRequest request, User currentUser);
}
