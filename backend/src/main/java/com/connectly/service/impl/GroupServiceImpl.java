package com.connectly.service.impl;

import com.connectly.constant.ChatType;
import com.connectly.constant.GroupRole;
import com.connectly.constant.MessageStatus;
import com.connectly.dto.request.AddGroupMembersRequest;
import com.connectly.dto.request.CreateGroupRequest;
import com.connectly.dto.request.UpdateGroupRequest;
import com.connectly.dto.response.GroupMemberResponse;
import com.connectly.dto.response.GroupResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.websocket.WsEvent;
import com.connectly.entity.*;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.exception.UnauthorizedException;
import com.connectly.repository.*;
import com.connectly.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupServiceImpl(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            ChatRepository chatRepository,
            ChatParticipantRepository participantRepository,
            MessageRepository messageRepository,
            MessageAttachmentRepository attachmentRepository,
            UserRepository userRepository,
            @Lazy SimpMessagingTemplate messagingTemplate
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, User currentUser) {
        // 1. Create underlying Chat container of type GROUP
        Chat chat = new Chat(ChatType.GROUP, request.getName().trim());
        chat.setDescription(request.getDescription());
        chat.setAvatarUrl(request.getAvatarUrl());
        chat = chatRepository.save(chat);

        // 2. Create Group entity
        Group group = new Group(chat, request.getName().trim(), currentUser);
        group.setDescription(request.getDescription());
        group = groupRepository.save(group);

        // 3. Add Creator as Admin
        participantRepository.save(new ChatParticipant(chat, currentUser));
        groupMemberRepository.save(new GroupMember(group, currentUser, GroupRole.ADMIN));

        // 4. Add initial members if provided
        Set<Long> addedUserIds = new HashSet<>();
        addedUserIds.add(currentUser.getId());

        if (request.getMemberUserIds() != null) {
            for (Long memberId : request.getMemberUserIds()) {
                if (memberId != null && !addedUserIds.contains(memberId)) {
                    User member = userRepository.findById(memberId).orElse(null);
                    if (member != null) {
                        participantRepository.save(new ChatParticipant(chat, member));
                        groupMemberRepository.save(new GroupMember(group, member, GroupRole.MEMBER));
                        addedUserIds.add(memberId);
                    }
                }
            }
        }

        logger.info("Group '{}' created successfully by {} with {} members", group.getName(), currentUser.getUsername(), addedUserIds.size());

        List<GroupMemberResponse> members = groupMemberRepository.findByGroupId(group.getId())
                .stream()
                .map(GroupMemberResponse::fromEntity)
                .collect(Collectors.toList());

        GroupResponse response = GroupResponse.fromEntity(group, members, true, null, 0);

        // Notify added members over WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/chat." + chat.getId(), new WsEvent<>("GROUP_CREATED", response));
        } catch (Exception e) {
            logger.warn("Could not broadcast group creation event: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(User currentUser) {
        List<GroupMember> myMemberships = groupMemberRepository.findByUserId(currentUser.getId());
        List<GroupResponse> results = new ArrayList<>();

        for (GroupMember gm : myMemberships) {
            Group group = gm.getGroup();
            Chat chat = group.getChat();

            List<GroupMemberResponse> members = groupMemberRepository.findByGroupId(group.getId())
                    .stream()
                    .map(GroupMemberResponse::fromEntity)
                    .collect(Collectors.toList());

            boolean isAdmin = gm.getRole() == GroupRole.ADMIN;
            MessageResponse lastMsg = getLastMessageResponse(chat.getId());
            long unread = messageRepository.countByChatIdAndSenderIdNotAndStatusNot(chat.getId(), currentUser.getId(), MessageStatus.READ);

            results.add(GroupResponse.fromEntity(group, members, isAdmin, lastMsg, unread));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupDetails(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        List<GroupMemberResponse> members = groupMemberRepository.findByGroupId(group.getId())
                .stream()
                .map(GroupMemberResponse::fromEntity)
                .collect(Collectors.toList());

        boolean isAdmin = currentMember.getRole() == GroupRole.ADMIN;
        MessageResponse lastMsg = getLastMessageResponse(group.getChat().getId());
        long unread = messageRepository.countByChatIdAndSenderIdNotAndStatusNot(group.getChat().getId(), currentUser.getId(), MessageStatus.READ);

        return GroupResponse.fromEntity(group, members, isAdmin, lastMsg, unread);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupByChatId(Long chatId, User currentUser) {
        Group group = groupRepository.findByChatId(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found for chat ID: " + chatId));

        return getGroupDetails(group.getId(), currentUser);
    }

    @Override
    @Transactional
    public GroupResponse updateGroupInfo(Long groupId, UpdateGroupRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Authorization rule: Only group admins can update group settings
        if (currentMember.getRole() != GroupRole.ADMIN) {
            throw new UnauthorizedException("Only group administrators can update group settings");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            group.setName(request.getName().trim());
            group.getChat().setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            group.setDescription(request.getDescription().trim());
            group.getChat().setDescription(request.getDescription().trim());
        }

        if (request.getAvatarUrl() != null) {
            group.getChat().setAvatarUrl(request.getAvatarUrl().trim());
        }

        chatRepository.save(group.getChat());
        group = groupRepository.save(group);

        List<GroupMemberResponse> members = groupMemberRepository.findByGroupId(group.getId())
                .stream()
                .map(GroupMemberResponse::fromEntity)
                .collect(Collectors.toList());

        MessageResponse lastMsg = getLastMessageResponse(group.getChat().getId());
        long unread = messageRepository.countByChatIdAndSenderIdNotAndStatusNot(group.getChat().getId(), currentUser.getId(), MessageStatus.READ);

        GroupResponse response = GroupResponse.fromEntity(group, members, true, lastMsg, unread);

        // Broadcast real-time group update to participants
        try {
            messagingTemplate.convertAndSend("/topic/chat." + group.getChat().getId(), new WsEvent<>("GROUP_UPDATED", response));
        } catch (Exception e) {
            logger.warn("Could not broadcast group update over WebSocket: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId, User currentUser) {
        verifyUserIsGroupMember(groupId, currentUser.getId());

        return groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(GroupMemberResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GroupMemberResponse> addMembers(Long groupId, AddGroupMembersRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Authorization: Only admin can add members
        if (currentMember.getRole() != GroupRole.ADMIN) {
            throw new UnauthorizedException("Only group administrators can add new members");
        }

        Chat chat = group.getChat();

        for (Long userId : request.getUserIds()) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    if (!participantRepository.existsByChatIdAndUserId(chat.getId(), userId)) {
                        participantRepository.save(new ChatParticipant(chat, user));
                    }
                    groupMemberRepository.save(new GroupMember(group, user, GroupRole.MEMBER));
                    logger.info("Added user {} to group {}", user.getUsername(), group.getName());
                }
            }
        }

        List<GroupMemberResponse> members = groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(GroupMemberResponse::fromEntity)
                .collect(Collectors.toList());

        // Broadcast member update over WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/chat." + chat.getId(), new WsEvent<>("GROUP_MEMBERS_UPDATED", members));
        } catch (Exception e) {
            logger.warn("Could not broadcast members update over WebSocket: {}", e.getMessage());
        }

        return members;
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userIdToRemove, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Senders can remove themselves (leave group). Removing other members requires ADMIN role.
        if (!userIdToRemove.equals(currentUser.getId())) {
            if (currentMember.getRole() != GroupRole.ADMIN) {
                throw new UnauthorizedException("Only group administrators can remove other members");
            }
        }

        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this group"));

        Chat chat = group.getChat();
        groupMemberRepository.delete(targetMember);

        participantRepository.findByChatIdAndUserId(chat.getId(), userIdToRemove)
                .ifPresent(participantRepository::delete);

        logger.info("Removed user ID {} from group {}", userIdToRemove, group.getName());

        Map<String, Object> payload = new HashMap<>();
        payload.put("groupId", groupId);
        payload.put("chatId", chat.getId());
        payload.put("userId", userIdToRemove);

        try {
            messagingTemplate.convertAndSend("/topic/chat." + chat.getId(), new WsEvent<>("GROUP_MEMBER_REMOVED", payload));
        } catch (Exception e) {
            logger.warn("Could not broadcast member removal over WebSocket: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void changeMemberRole(Long groupId, Long targetUserId, GroupRole newRole, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupRole.ADMIN) {
            throw new UnauthorizedException("Only group administrators can change member roles");
        }

        GroupMember targetMember = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this group"));

        targetMember.setRole(newRole);
        groupMemberRepository.save(targetMember);
        logger.info("Changed role of user ID {} in group {} to {}", targetUserId, group.getName(), newRole);
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, User currentUser) {
        removeMember(groupId, currentUser.getId(), currentUser);
    }

    private void verifyUserIsGroupMember(Long groupId, Long userId) {
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
        if (!isMember) {
            throw new UnauthorizedException("You are not a member of this group");
        }
    }

    private MessageResponse getLastMessageResponse(Long chatId) {
        Optional<Message> lastMsg = messageRepository.findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chatId);
        if (lastMsg.isEmpty()) return null;
        Message msg = lastMsg.get();
        List<MessageAttachment> atts = attachmentRepository.findByMessageId(msg.getId());
        return MessageResponse.fromEntity(msg, atts.isEmpty() ? null : atts.get(0));
    }
}
