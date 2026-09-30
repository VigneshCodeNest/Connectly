package com.connectly.dto.response;

import com.connectly.constant.GroupRole;
import com.connectly.entity.GroupMember;

import java.time.LocalDateTime;

public class GroupMemberResponse {
    private Long id;
    private UserSummaryResponse user;
    private GroupRole role;
    private LocalDateTime joinedAt;

    public GroupMemberResponse() {}

    public static GroupMemberResponse fromEntity(GroupMember gm) {
        if (gm == null) return null;
        GroupMemberResponse res = new GroupMemberResponse();
        res.setId(gm.getId());
        res.setUser(UserSummaryResponse.fromEntity(gm.getUser()));
        res.setRole(gm.getRole());
        res.setJoinedAt(gm.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserSummaryResponse getUser() { return user; }
    public void setUser(UserSummaryResponse user) { this.user = user; }

    public GroupRole getRole() { return role; }
    public void setRole(GroupRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
