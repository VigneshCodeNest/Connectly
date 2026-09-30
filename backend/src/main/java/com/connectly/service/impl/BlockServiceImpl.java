package com.connectly.service.impl;

import com.connectly.dto.response.BlockedUserResponse;
import com.connectly.entity.BlockedUser;
import com.connectly.entity.User;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.repository.BlockedUserRepository;
import com.connectly.repository.UserRepository;
import com.connectly.service.BlockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlockServiceImpl implements BlockService {

    private static final Logger logger = LoggerFactory.getLogger(BlockServiceImpl.class);

    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;

    public BlockServiceImpl(BlockedUserRepository blockedUserRepository, UserRepository userRepository) {
        this.blockedUserRepository = blockedUserRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BlockedUserResponse blockUser(Long targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new BadRequestException("You cannot block yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + targetUserId));

        Optional<BlockedUser> existing = blockedUserRepository.findByUserIdAndBlockedUserId(currentUser.getId(), targetUserId);
        if (existing.isPresent()) {
            logger.info("User {} already blocked user {}", currentUser.getUsername(), targetUser.getUsername());
            return BlockedUserResponse.fromEntity(existing.get());
        }

        BlockedUser blockedUser = new BlockedUser(currentUser, targetUser);
        BlockedUser saved = blockedUserRepository.save(blockedUser);
        logger.info("User {} blocked user {}", currentUser.getUsername(), targetUser.getUsername());

        return BlockedUserResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void unblockUser(Long targetUserId, User currentUser) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new BadRequestException("Invalid operation: cannot unblock yourself");
        }

        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("User not found with ID: " + targetUserId);
        }

        blockedUserRepository.deleteByUserIdAndBlockedUserId(currentUser.getId(), targetUserId);
        logger.info("User {} unblocked user ID {}", currentUser.getUsername(), targetUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(User currentUser) {
        return blockedUserRepository.findByUserId(currentUser.getId())
                .stream()
                .map(BlockedUserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlockedBetween(Long user1Id, Long user2Id) {
        if (user1Id == null || user2Id == null) return false;
        return blockedUserRepository.existsByUserIdAndBlockedUserId(user1Id, user2Id)
                || blockedUserRepository.existsByUserIdAndBlockedUserId(user2Id, user1Id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlockedBy(Long blockerId, Long blockedId) {
        if (blockerId == null || blockedId == null) return false;
        return blockedUserRepository.existsByUserIdAndBlockedUserId(blockerId, blockedId);
    }
}
