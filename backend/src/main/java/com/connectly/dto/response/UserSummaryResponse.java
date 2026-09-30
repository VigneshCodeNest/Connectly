package com.connectly.dto.response;

import com.connectly.entity.User;
import java.time.LocalDateTime;

public class UserSummaryResponse {
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

    public UserSummaryResponse() {}

    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) return null;
        UserSummaryResponse dto = new UserSummaryResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBio(user.getBio());
        dto.setColorAccent(user.getColorAccent());
        dto.setOnline(user.isOnline());
        dto.setLastSeenPrivacy(user.getLastSeenPrivacy());
        dto.setProfilePhotoPrivacy(user.getProfilePhotoPrivacy());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
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
}
