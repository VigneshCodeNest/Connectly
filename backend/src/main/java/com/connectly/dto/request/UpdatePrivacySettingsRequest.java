package com.connectly.dto.request;

import jakarta.validation.constraints.Pattern;

public class UpdatePrivacySettingsRequest {

    @Pattern(regexp = "^(Everyone|My Contacts|Nobody)$", message = "lastSeenPrivacy must be 'Everyone', 'My Contacts', or 'Nobody'")
    private String lastSeenPrivacy;

    @Pattern(regexp = "^(Everyone|My Contacts|Nobody)$", message = "profilePhotoPrivacy must be 'Everyone', 'My Contacts', or 'Nobody'")
    private String profilePhotoPrivacy;

    public UpdatePrivacySettingsRequest() {}

    public UpdatePrivacySettingsRequest(String lastSeenPrivacy, String profilePhotoPrivacy) {
        this.lastSeenPrivacy = lastSeenPrivacy;
        this.profilePhotoPrivacy = profilePhotoPrivacy;
    }

    public String getLastSeenPrivacy() { return lastSeenPrivacy; }
    public void setLastSeenPrivacy(String lastSeenPrivacy) { this.lastSeenPrivacy = lastSeenPrivacy; }

    public String getProfilePhotoPrivacy() { return profilePhotoPrivacy; }
    public void setProfilePhotoPrivacy(String profilePhotoPrivacy) { this.profilePhotoPrivacy = profilePhotoPrivacy; }
}
