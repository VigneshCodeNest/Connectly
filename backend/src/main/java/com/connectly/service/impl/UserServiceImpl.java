package com.connectly.service.impl;

import com.connectly.dto.request.UpdateAvatarRequest;
import com.connectly.dto.request.UpdateBioRequest;
import com.connectly.dto.request.UpdateProfileRequest;
import com.connectly.dto.request.UpdateUsernameRequest;
import com.connectly.dto.response.UserProfileResponse;
import com.connectly.dto.response.UserSearchResponse;
import com.connectly.entity.User;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.repository.UserRepository;
import com.connectly.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(User currentUser) {
        return UserProfileResponse.fromEntity(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return UserProfileResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSearchResponse searchUserByEmail(String email, User currentUser) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Please enter a valid email address");
        }

        String cleanEmail = email.trim().toLowerCase();
        User targetUser = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + cleanEmail));

        boolean isSelf = targetUser.getId().equals(currentUser.getId());
        return UserSearchResponse.fromEntity(targetUser, isSelf);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request, User currentUser) {
        currentUser.setName(request.getName().trim());
        if (request.getBio() != null) {
            currentUser.setBio(request.getBio().trim());
        }
        if (request.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getColorAccent() != null) {
            currentUser.setColorAccent(request.getColorAccent().trim());
        }

        User updated = userRepository.save(currentUser);
        return UserProfileResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUsername(UpdateUsernameRequest request, User currentUser) {
        String newUsername = request.getUsername().trim();
        if (!newUsername.startsWith("@")) {
            newUsername = "@" + newUsername;
        }

        // If username is not changed, return current
        if (newUsername.equalsIgnoreCase(currentUser.getUsername())) {
            return UserProfileResponse.fromEntity(currentUser);
        }

        // Check uniqueness
        if (userRepository.existsByUsernameIgnoreCase(newUsername)) {
            throw new BadRequestException("Username " + newUsername + " is already taken");
        }

        currentUser.setUsername(newUsername);
        User updated = userRepository.save(currentUser);
        return UserProfileResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(UpdateAvatarRequest request, User currentUser) {
        currentUser.setAvatarUrl(request.getAvatarUrl().trim());
        User updated = userRepository.save(currentUser);
        return UserProfileResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public UserProfileResponse updateBio(UpdateBioRequest request, User currentUser) {
        currentUser.setBio(request.getBio() != null ? request.getBio().trim() : "");
        User updated = userRepository.save(currentUser);
        return UserProfileResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void changePassword(com.connectly.dto.request.ChangePasswordRequest request, User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation password do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user {}", user.getUsername());
    }

    @Override
    @Transactional
    public UserProfileResponse updatePrivacySettings(com.connectly.dto.request.UpdatePrivacySettingsRequest request, User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getLastSeenPrivacy() != null) {
            user.setLastSeenPrivacy(request.getLastSeenPrivacy());
        }
        if (request.getProfilePhotoPrivacy() != null) {
            user.setProfilePhotoPrivacy(request.getProfilePhotoPrivacy());
        }

        User updated = userRepository.save(user);
        logger.info("Privacy settings updated for user {}", user.getUsername());
        return UserProfileResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public UserProfileResponse updateSettings(com.connectly.dto.request.UpdateSettingsRequest request, User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getColorAccent() != null && !request.getColorAccent().trim().isEmpty()) {
            user.setColorAccent(request.getColorAccent().trim());
        }
        if (request.getLastSeenPrivacy() != null) {
            user.setLastSeenPrivacy(request.getLastSeenPrivacy());
        }
        if (request.getProfilePhotoPrivacy() != null) {
            user.setProfilePhotoPrivacy(request.getProfilePhotoPrivacy());
        }

        User updated = userRepository.save(user);
        logger.info("Settings updated for user {}", user.getUsername());
        return UserProfileResponse.fromEntity(updated);
    }
}
