package com.connectly.dto.response;

import com.connectly.entity.User;

public class UserSearchResponse {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String avatarUrl;
    private String bio;
    private boolean isOnline;
    private boolean isSelf;

    public UserSearchResponse() {}

    public static UserSearchResponse fromEntity(User user, boolean isSelf) {
        if (user == null) return null;
        UserSearchResponse res = new UserSearchResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setUsername(user.getUsername());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setBio(user.getBio());
        res.setOnline(user.isOnline());
        res.setSelf(isSelf);
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

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public boolean isSelf() { return isSelf; }
    public void setSelf(boolean self) { isSelf = self; }
}
