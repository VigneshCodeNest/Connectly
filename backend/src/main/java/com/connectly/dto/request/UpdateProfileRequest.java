package com.connectly.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 300, message = "Bio cannot exceed 300 characters")
    private String bio;

    @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
    private String avatarUrl;

    @Size(max = 30, message = "Color accent cannot exceed 30 characters")
    private String colorAccent;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String name, String bio, String avatarUrl) {
        this.name = name;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getColorAccent() { return colorAccent; }
    public void setColorAccent(String colorAccent) { this.colorAccent = colorAccent; }
}
