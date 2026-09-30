package com.connectly.service;

import com.connectly.dto.response.BlockedUserResponse;
import com.connectly.entity.User;

import java.util.List;

public interface BlockService {

    BlockedUserResponse blockUser(Long targetUserId, User currentUser);

    void unblockUser(Long targetUserId, User currentUser);

    List<BlockedUserResponse> getBlockedUsers(User currentUser);

    boolean isBlockedBetween(Long user1Id, Long user2Id);

    boolean isBlockedBy(Long blockerId, Long blockedId);
}
