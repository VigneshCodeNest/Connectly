package com.connectly.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateBioRequest {

    @Size(max = 300, message = "Bio cannot exceed 300 characters")
    private String bio;

    public UpdateBioRequest() {}

    public UpdateBioRequest(String bio) {
        this.bio = bio;
    }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
