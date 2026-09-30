package com.connectly.dto.response;

import com.connectly.entity.User;
import java.time.LocalDateTime;

public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String avatarUrl;
    private String bio;
    private String colorAccent;
    private boolean isOnline;
    private String lastSeenPrivacy;
    private String profilePhotoPrivacy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileResponse() {}

    public static UserProfileResponse fromEntity(User user) {
        if (user == null) return null;
        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setUsername(user.getUsername());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setBio(user.getBio());
        res.setColorAccent(user.getColorAccent());
        res.setOnline(user.isOnline());
        res.setLastSeenPrivacy(user.getLastSeenPrivacy());
        res.setProfilePhotoPrivacy(user.getProfilePhotoPrivacy());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getColorAccent() { return colorAccent; }
    public void setColorAccent(String colorAccent) { this.colorAccent = colorAccent; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getLastSeenPrivacy() { return lastSeenPrivacy; }
    public void setLastSeenPrivacy(String lastSeenPrivacy) { this.lastSeenPrivacy = lastSeenPrivacy; }

    public String getProfilePhotoPrivacy() { return profilePhotoPrivacy; }
    public void setProfilePhotoPrivacy(String profilePhotoPrivacy) { this.profilePhotoPrivacy = profilePhotoPrivacy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
