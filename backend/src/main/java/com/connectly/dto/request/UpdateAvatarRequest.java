package com.connectly.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateAvatarRequest {

    @NotBlank(message = "Avatar URL is required")
    @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
    private String avatarUrl;

    public UpdateAvatarRequest() {}

    public UpdateAvatarRequest(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
