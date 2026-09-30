package com.connectly.dto.request;

import com.connectly.constant.GroupRole;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private GroupRole role;

    public UpdateMemberRoleRequest() {}

    public UpdateMemberRoleRequest(GroupRole role) {
        this.role = role;
    }

    public GroupRole getRole() { return role; }
    public void setRole(GroupRole role) { this.role = role; }
}
