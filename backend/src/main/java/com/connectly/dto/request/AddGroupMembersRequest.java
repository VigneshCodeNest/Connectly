package com.connectly.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class AddGroupMembersRequest {

    @NotEmpty(message = "User IDs list cannot be empty")
    private List<Long> userIds = new ArrayList<>();

    public AddGroupMembersRequest() {}

    public AddGroupMembersRequest(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
}
