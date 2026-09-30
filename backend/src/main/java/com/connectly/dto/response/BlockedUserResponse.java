package com.connectly.dto.response;

import com.connectly.entity.BlockedUser;

import java.time.LocalDateTime;

public class BlockedUserResponse {

    private Long id;
    private UserSummaryResponse blockedUser;
    private LocalDateTime blockedAt;

    public BlockedUserResponse() {}

    public BlockedUserResponse(Long id, UserSummaryResponse blockedUser, LocalDateTime blockedAt) {
        this.id = id;
        this.blockedUser = blockedUser;
        this.blockedAt = blockedAt;
    }

    public static BlockedUserResponse fromEntity(BlockedUser entity) {
        if (entity == null) return null;
        return new BlockedUserResponse(
                entity.getId(),
                UserSummaryResponse.fromEntity(entity.getBlockedUser()),
                entity.getCreatedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserSummaryResponse getBlockedUser() { return blockedUser; }
    public void setBlockedUser(UserSummaryResponse blockedUser) { this.blockedUser = blockedUser; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }
}
