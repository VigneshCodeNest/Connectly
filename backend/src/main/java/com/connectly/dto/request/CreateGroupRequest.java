package com.connectly.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 1, max = 120, message = "Group name must be between 1 and 120 characters")
    private String name;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    private String avatarUrl;

    private List<Long> memberUserIds = new ArrayList<>();

    public CreateGroupRequest() {}

    public CreateGroupRequest(String name, String description, List<Long> memberUserIds) {
        this.name = name;
        this.description = description;
        this.memberUserIds = memberUserIds != null ? memberUserIds : new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public List<Long> getMemberUserIds() { return memberUserIds; }
    public void setMemberUserIds(List<Long> memberUserIds) { this.memberUserIds = memberUserIds; }
}
