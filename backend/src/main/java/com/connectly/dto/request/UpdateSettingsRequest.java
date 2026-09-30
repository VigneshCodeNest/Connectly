package com.connectly.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateSettingsRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 300, message = "Bio must not exceed 300 characters")
    private String bio;

    @Size(max = 30, message = "Color accent must not exceed 30 characters")
    private String colorAccent;

    private String lastSeenPrivacy;
    private String profilePhotoPrivacy;

    public UpdateSettingsRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getColorAccent() { return colorAccent; }
    public void setColorAccent(String colorAccent) { this.colorAccent = colorAccent; }

    public String getLastSeenPrivacy() { return lastSeenPrivacy; }
    public void setLastSeenPrivacy(String lastSeenPrivacy) { this.lastSeenPrivacy = lastSeenPrivacy; }

    public String getProfilePhotoPrivacy() { return profilePhotoPrivacy; }
    public void setProfilePhotoPrivacy(String profilePhotoPrivacy) { this.profilePhotoPrivacy = profilePhotoPrivacy; }
}
