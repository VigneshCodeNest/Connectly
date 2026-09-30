package com.connectly.service.impl;

import com.connectly.constant.ChatType;
import com.connectly.constant.MessageStatus;
import com.connectly.constant.MessageType;
import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.ForwardMessageRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ChatResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.response.UserSummaryResponse;
import com.connectly.dto.websocket.WsEvent;
import com.connectly.dto.websocket.WsStatusUpdatePayload;
import com.connectly.entity.*;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.ResourceNotFoundException;
import com.connectly.exception.UnauthorizedException;
import com.connectly.repository.*;
import com.connectly.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final StarredMessageRepository starredMessageRepository;
    private final UserRepository userRepository;
    private final ConnectionRequestRepository connectionRepository;
    private final com.connectly.repository.BlockedUserRepository blockedUserRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(
            ChatRepository chatRepository,
            ChatParticipantRepository participantRepository,
            MessageRepository messageRepository,
            MessageAttachmentRepository attachmentRepository,
            StarredMessageRepository starredMessageRepository,
            UserRepository userRepository,
            ConnectionRequestRepository connectionRepository,
            com.connectly.repository.BlockedUserRepository blockedUserRepository,
            @Lazy SimpMessagingTemplate messagingTemplate
    ) {
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.starredMessageRepository = starredMessageRepository;
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public ChatResponse getOrCreateDirectChat(CreateDirectChatRequest request, User currentUser) {
        Long targetUserId = request.getTargetUserId();

        if (currentUser.getId().equals(targetUserId)) {
            throw new BadRequestException("You cannot start a private chat with yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + targetUserId));

        // Blocking communication rule enforcement
        if (blockedUserRepository.existsByUserIdAndBlockedUserId(currentUser.getId(), targetUserId) ||
            blockedUserRepository.existsByUserIdAndBlockedUserId(targetUserId, currentUser.getId())) {
            throw new BadRequestException("Cannot create or open private chat. Communication is blocked between these users.");
        }

        // CRITICAL AUTHORIZATION RULE: A private chat can ONLY exist between users who have an ACCEPTED connection
        boolean connected = connectionRepository.areConnected(currentUser.getId(), targetUserId);
        if (!connected) {
            throw new BadRequestException("You cannot start a private conversation with " + targetUser.getName() + " because you are not connected. Please send and have a connection request accepted first.");
        }

        // Check if direct chat already exists
        Optional<Chat> existingChat = chatRepository.findDirectChatBetweenUsers(currentUser.getId(), targetUserId);
        Chat chat;
        if (existingChat.isPresent()) {
            chat = existingChat.get();
        } else {
            // Create new Direct Chat container
            chat = new Chat(ChatType.DIRECT, targetUser.getName());
            chat = chatRepository.save(chat);

            // Add both users as participants
            participantRepository.save(new ChatParticipant(chat, currentUser));
            participantRepository.save(new ChatParticipant(chat, targetUser));
        }

        MessageResponse lastMsg = getLastMessageResponse(chat.getId());
        long unread = messageRepository.countByChatIdAndSenderIdNotAndStatusNot(chat.getId(), currentUser.getId(), MessageStatus.READ);
        ChatParticipant myCp = participantRepository.findByChatIdAndUserId(chat.getId(), currentUser.getId()).orElse(null);
        boolean isMuted = myCp != null && myCp.isCurrentlyMuted();
        java.time.LocalDateTime mutedUntil = myCp != null ? myCp.getMutedUntil() : null;

        return ChatResponse.fromEntity(chat, targetUser, lastMsg, unread, isMuted, mutedUntil);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getUserChats(User currentUser) {
        List<ChatParticipant> myParticipants = participantRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId());
        List<ChatResponse> results = new ArrayList<>();

        for (ChatParticipant cp : myParticipants) {
            Chat chat = cp.getChat();
            User otherUser = null;

            if (chat.getType() == ChatType.DIRECT) {
                List<ChatParticipant> allParticipants = participantRepository.findByChatId(chat.getId());
                for (ChatParticipant p : allParticipants) {
                    if (!p.getUser().getId().equals(currentUser.getId())) {
                        otherUser = p.getUser();
                        break;
                    }
                }
            }

            MessageResponse lastMsg = getLastMessageResponse(chat.getId());
            long unread = messageRepository.countByChatIdAndSenderIdNotAndStatusNot(chat.getId(), currentUser.getId(), MessageStatus.READ);
            results.add(ChatResponse.fromEntity(chat, otherUser, lastMsg, unread, cp.isCurrentlyMuted(), cp.getMutedUntil()));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatDetails(Long chatId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found with ID: " + chatId));

        User otherUser = null;
        if (chat.getType() == ChatType.DIRECT) {
            List<ChatParticipant> allParticipants = participantRepository.findByChatId(chat.getId());
            for (ChatParticipant p : allParticipants) {
                if (!p.getUser().getId().equals(currentUser.getId())) {
                    otherUser = p.getUser();
                    break;
                }
            }
        }

        MessageResponse lastMsg = getLastMessageResponse(chat.getId());
        long unread = messageRepository.countByChatIdAndSenderIdNotAndStatusNot(chat.getId(), currentUser.getId(), MessageStatus.READ);
        ChatParticipant myCp = participantRepository.findByChatIdAndUserId(chatId, currentUser.getId()).orElse(null);
        boolean isMuted = myCp != null && myCp.isCurrentlyMuted();
        java.time.LocalDateTime mutedUntil = myCp != null ? myCp.getMutedUntil() : null;

        return ChatResponse.fromEntity(chat, otherUser, lastMsg, unread, isMuted, mutedUntil);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getChatParticipants(Long chatId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        return participantRepository.findByChatId(chatId)
                .stream()
                .map(cp -> UserSummaryResponse.fromEntity(cp.getUser()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getChatMessages(Long chatId, Pageable pageable, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        Set<Long> starredMessageIds = starredMessageRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(sm -> sm.getMessage().getId())
                .collect(Collectors.toSet());

        return messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatId, pageable)
                .map(msg -> {
                    List<MessageAttachment> atts = attachmentRepository.findByMessageId(msg.getId());
                    MessageAttachment att = atts.isEmpty() ? null : atts.get(0);
                    boolean isStarred = starredMessageIds.contains(msg.getId());
                    return MessageResponse.fromEntity(msg, att, isStarred);
                });
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long chatId, SendMessageRequest request, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found with ID: " + chatId));

        // For direct chats, ensure users remain connected and not blocked
        if (chat.getType() == ChatType.DIRECT) {
            List<ChatParticipant> participants = participantRepository.findByChatId(chatId);
            for (ChatParticipant p : participants) {
                if (!p.getUser().getId().equals(currentUser.getId())) {
                    if (blockedUserRepository.existsByUserIdAndBlockedUserId(currentUser.getId(), p.getUser().getId()) ||
                        blockedUserRepository.existsByUserIdAndBlockedUserId(p.getUser().getId(), currentUser.getId())) {
                        throw new BadRequestException("Cannot send message. Communication is blocked between these users.");
                    }
                    boolean connected = connectionRepository.areConnected(currentUser.getId(), p.getUser().getId());
                    if (!connected) {
                        throw new BadRequestException("Cannot send message. You are no longer connected with " + p.getUser().getName());
                    }
                }
            }
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSender(currentUser);

        MessageType msgType = request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT;
        if (msgType == MessageType.TEXT && request.getFileUrl() != null && !request.getFileUrl().trim().isEmpty()) {
            if (request.getFileType() != null && request.getFileType().startsWith("video/")) {
                msgType = MessageType.VIDEO;
            } else if (request.getFileType() != null && request.getFileType().startsWith("image/")) {
                msgType = MessageType.IMAGE;
            }
        }

        message.setContent(request.getContent() != null ? request.getContent() : "");
        message.setMessageType(msgType);
        message.setStatus(MessageStatus.SENT);
        message.setForwarded(request.isForwarded());

        // Reply hierarchy: Message must reference another message in the SAME conversation
        if (request.getReplyToId() != null) {
            Message replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reply target message not found with ID: " + request.getReplyToId()));

            if (!replyTo.getChat().getId().equals(chatId)) {
                throw new BadRequestException("Cannot reply to a message from a different conversation");
            }
            message.setReplyTo(replyTo);
        }

        Message savedMessage = messageRepository.save(message);

        // Attachment metadata
        MessageAttachment savedAttachment = null;
        if (request.getFileUrl() != null && !request.getFileUrl().trim().isEmpty()) {
            MessageAttachment attachment = new MessageAttachment();
            attachment.setMessage(savedMessage);
            attachment.setFileUrl(request.getFileUrl().trim());
            attachment.setFileName(request.getFileName());
            attachment.setFileType(request.getFileType());
            attachment.setFileSize(request.getFileSize());
            savedAttachment = attachmentRepository.save(attachment);
        }

        MessageResponse response = MessageResponse.fromEntity(savedMessage, savedAttachment);

        // Broadcast new message in real-time over WebSocket destination /topic/chat.{chatId}
        try {
            messagingTemplate.convertAndSend("/topic/chat." + chatId, new WsEvent<>("NEW_MESSAGE", response));
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, new WsEvent<>("NEW_MESSAGE", response));
        } catch (Exception e) {
            logger.warn("Could not broadcast real-time message to WebSocket topic: {}", e.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public List<MessageResponse> forwardMessage(Long sourceChatId, Long messageId, ForwardMessageRequest request, User currentUser) {
        verifyUserIsParticipant(sourceChatId, currentUser.getId());

        Message sourceMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Source message not found with ID: " + messageId));

        if (!sourceMessage.getChat().getId().equals(sourceChatId)) {
            throw new BadRequestException("Source message does not belong to specified chat");
        }

        if (sourceMessage.isDeleted()) {
            throw new BadRequestException("Cannot forward a deleted message");
        }

        List<MessageAttachment> sourceAttachments = attachmentRepository.findByMessageId(sourceMessage.getId());
        MessageAttachment sourceAttachment = sourceAttachments.isEmpty() ? null : sourceAttachments.get(0);

        Set<Long> targetChatIds = new HashSet<>(request.getTargetChatIds());

        // Resolve direct chats from target user IDs if provided
        for (Long targetUserId : request.getTargetUserIds()) {
            if (!targetUserId.equals(currentUser.getId())) {
                CreateDirectChatRequest directReq = new CreateDirectChatRequest();
                directReq.setTargetUserId(targetUserId);
                ChatResponse chatRes = getOrCreateDirectChat(directReq, currentUser);
                targetChatIds.add(chatRes.getId());
            }
        }

        if (targetChatIds.isEmpty()) {
            throw new BadRequestException("At least one target conversation or connected user must be specified for forwarding");
        }

        List<MessageResponse> forwardedResponses = new ArrayList<>();

        for (Long targetChatId : targetChatIds) {
            verifyUserIsParticipant(targetChatId, currentUser.getId());

            Chat targetChat = chatRepository.findById(targetChatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target chat not found with ID: " + targetChatId));

            // Ensure connection for direct chats
            if (targetChat.getType() == ChatType.DIRECT) {
                List<ChatParticipant> participants = participantRepository.findByChatId(targetChatId);
                for (ChatParticipant p : participants) {
                    if (!p.getUser().getId().equals(currentUser.getId())) {
                        boolean connected = connectionRepository.areConnected(currentUser.getId(), p.getUser().getId());
                        if (!connected) {
                            throw new BadRequestException("Cannot forward message. You are no longer connected with " + p.getUser().getName());
                        }
                    }
                }
            }

            Message forwardMessage = new Message();
            forwardMessage.setChat(targetChat);
            forwardMessage.setSender(currentUser);
            forwardMessage.setContent(sourceMessage.getContent());
            forwardMessage.setMessageType(sourceMessage.getMessageType());
            forwardMessage.setStatus(MessageStatus.SENT);
            forwardMessage.setForwarded(true);

            Message savedForward = messageRepository.save(forwardMessage);

            MessageAttachment savedAttachment = null;
            if (sourceAttachment != null) {
                MessageAttachment att = new MessageAttachment();
                att.setMessage(savedForward);
                att.setFileUrl(sourceAttachment.getFileUrl());
                att.setFileName(sourceAttachment.getFileName());
                att.setFileType(sourceAttachment.getFileType());
                att.setFileSize(sourceAttachment.getFileSize());
                savedAttachment = attachmentRepository.save(att);
            }

            MessageResponse response = MessageResponse.fromEntity(savedForward, savedAttachment);
            forwardedResponses.add(response);

            // Broadcast real-time message to target conversation
            try {
                messagingTemplate.convertAndSend("/topic/chat." + targetChatId, new WsEvent<>("NEW_MESSAGE", response));
                messagingTemplate.convertAndSend("/topic/chat/" + targetChatId, new WsEvent<>("NEW_MESSAGE", response));
            } catch (Exception e) {
                logger.warn("Could not broadcast forwarded message to WebSocket: {}", e.getMessage());
            }
        }

        return forwardedResponses;
    }

    @Override
    @Transactional
    public void deleteMessage(Long chatId, Long messageId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        if (!message.getChat().getId().equals(chatId)) {
            throw new BadRequestException("Message does not belong to specified chat");
        }

        // Authorization rule: Only the sender of the message can delete it
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete messages sent by another user");
        }

        message.setDeleted(true);
        messageRepository.save(message);
        logger.info("Message {} soft-deleted by user {}", messageId, currentUser.getUsername());

        // Broadcast real-time delete event to all participants
        Map<String, Object> deletePayload = new HashMap<>();
        deletePayload.put("chatId", chatId);
        deletePayload.put("messageId", messageId);

        try {
            messagingTemplate.convertAndSend("/topic/chat." + chatId, new WsEvent<>("MESSAGE_DELETED", deletePayload));
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, new WsEvent<>("MESSAGE_DELETED", deletePayload));
        } catch (Exception e) {
            logger.warn("Could not broadcast message delete over WebSocket: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void starMessage(Long chatId, Long messageId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        if (!message.getChat().getId().equals(chatId)) {
            throw new BadRequestException("Message does not belong to specified chat");
        }

        if (!starredMessageRepository.existsByUserIdAndMessageId(currentUser.getId(), messageId)) {
            StarredMessage starred = new StarredMessage(currentUser, message);
            starredMessageRepository.save(starred);
            logger.info("Message {} starred by user {}", messageId, currentUser.getUsername());
        }
    }

    @Override
    @Transactional
    public void unstarMessage(Long chatId, Long messageId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());
        starredMessageRepository.deleteByUserIdAndMessageId(currentUser.getId(), messageId);
        logger.info("Message {} unstarred by user {}", messageId, currentUser.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getStarredMessages(User currentUser) {
        List<StarredMessage> starredList = starredMessageRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        List<MessageResponse> results = new ArrayList<>();

        for (StarredMessage sm : starredList) {
            Message msg = sm.getMessage();
            if (!msg.isDeleted()) {
                List<MessageAttachment> atts = attachmentRepository.findByMessageId(msg.getId());
                MessageAttachment att = atts.isEmpty() ? null : atts.get(0);
                results.add(MessageResponse.fromEntity(msg, att, true));
            }
        }

        return results;
    }

    @Override
    @Transactional
    public void markChatMessagesAsRead(Long chatId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        int updatedCount = messageRepository.updateStatusForReceivedMessages(chatId, currentUser.getId(), MessageStatus.READ);
        logger.info("Marked {} messages as READ in chat {} by user {}", updatedCount, chatId, currentUser.getUsername());

        // Broadcast READ status receipt in real-time
        WsStatusUpdatePayload payload = new WsStatusUpdatePayload(chatId, null, MessageStatus.READ, currentUser.getId());
        try {
            messagingTemplate.convertAndSend("/topic/chat." + chatId, new WsEvent<>("MESSAGE_READ", payload));
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, new WsEvent<>("MESSAGE_READ", payload));
        } catch (Exception e) {
            logger.warn("Could not broadcast read receipt over WebSocket: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void markMessageAsDelivered(Long chatId, Long messageId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        int updatedCount = messageRepository.updateSingleMessageStatus(chatId, messageId, currentUser.getId(), MessageStatus.DELIVERED);
        if (updatedCount > 0) {
            logger.info("Marked message {} as DELIVERED in chat {} by user {}", messageId, chatId, currentUser.getUsername());
            WsStatusUpdatePayload payload = new WsStatusUpdatePayload(chatId, messageId, MessageStatus.DELIVERED, currentUser.getId());
            try {
                messagingTemplate.convertAndSend("/topic/chat." + chatId, new WsEvent<>("MESSAGE_DELIVERED", payload));
                messagingTemplate.convertAndSend("/topic/chat/" + chatId, new WsEvent<>("MESSAGE_DELIVERED", payload));
            } catch (Exception e) {
                logger.warn("Could not broadcast delivery receipt over WebSocket: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void markAllChatMessagesAsDelivered(Long chatId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        // Update all SENT messages to DELIVERED where sender is not currentUser
        List<Message> sentMessages = messageRepository.findByChatIdAndSenderIdNotAndStatus(chatId, currentUser.getId(), MessageStatus.SENT);
        for (Message m : sentMessages) {
            m.setStatus(MessageStatus.DELIVERED);
        }
        messageRepository.saveAll(sentMessages);

        if (!sentMessages.isEmpty()) {
            logger.info("Marked {} messages as DELIVERED in chat {} by user {}", sentMessages.size(), chatId, currentUser.getUsername());
            WsStatusUpdatePayload payload = new WsStatusUpdatePayload(chatId, null, MessageStatus.DELIVERED, currentUser.getId());
            try {
                messagingTemplate.convertAndSend("/topic/chat." + chatId, new WsEvent<>("MESSAGE_DELIVERED", payload));
                messagingTemplate.convertAndSend("/topic/chat/" + chatId, new WsEvent<>("MESSAGE_DELIVERED", payload));
            } catch (Exception e) {
                logger.warn("Could not broadcast delivery receipts over WebSocket: {}", e.getMessage());
            }
        }
    }

    private void verifyUserIsParticipant(Long chatId, Long userId) {
        boolean isParticipant = participantRepository.existsByChatIdAndUserId(chatId, userId);
        if (!isParticipant) {
            throw new UnauthorizedException("You are not a participant in this conversation");
        }
    }

    private MessageResponse getLastMessageResponse(Long chatId) {
        Optional<Message> lastMsg = messageRepository.findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(chatId);
        if (lastMsg.isEmpty()) return null;
        Message msg = lastMsg.get();
        List<MessageAttachment> atts = attachmentRepository.findByMessageId(msg.getId());
        return MessageResponse.fromEntity(msg, atts.isEmpty() ? null : atts.get(0));
    }

    @Override
    @Transactional
    public com.connectly.dto.response.ChatPreferencesResponse muteChat(Long chatId, com.connectly.dto.request.MuteChatRequest request, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        ChatParticipant participant = participantRepository.findByChatIdAndUserId(chatId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant record not found"));

        participant.setMuted(true);
        participant.setMutedUntil(request != null ? request.calculateMutedUntil() : null);
        participantRepository.save(participant);

        logger.info("User {} muted chat {} until {}", currentUser.getUsername(), chatId, participant.getMutedUntil());
        return getChatPreferences(chatId, currentUser);
    }

    @Override
    @Transactional
    public com.connectly.dto.response.ChatPreferencesResponse unmuteChat(Long chatId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        ChatParticipant participant = participantRepository.findByChatIdAndUserId(chatId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant record not found"));

        participant.setMuted(false);
        participant.setMutedUntil(null);
        participantRepository.save(participant);

        logger.info("User {} unmuted chat {}", currentUser.getUsername(), chatId);
        return getChatPreferences(chatId, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public com.connectly.dto.response.ChatPreferencesResponse getChatPreferences(Long chatId, User currentUser) {
        verifyUserIsParticipant(chatId, currentUser.getId());

        ChatParticipant cp = participantRepository.findByChatIdAndUserId(chatId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant record not found"));

        Chat chat = cp.getChat();
        User otherUser = null;
        boolean isBlocked = false;

        if (chat.getType() == ChatType.DIRECT) {
            List<ChatParticipant> allParticipants = participantRepository.findByChatId(chatId);
            for (ChatParticipant p : allParticipants) {
                if (!p.getUser().getId().equals(currentUser.getId())) {
                    otherUser = p.getUser();
                    isBlocked = blockedUserRepository.existsByUserIdAndBlockedUserId(currentUser.getId(), otherUser.getId()) ||
                                blockedUserRepository.existsByUserIdAndBlockedUserId(otherUser.getId(), currentUser.getId());
                    break;
                }
            }
        }

        return new com.connectly.dto.response.ChatPreferencesResponse(
                chatId,
                cp.isCurrentlyMuted(),
                cp.getMutedUntil(),
                isBlocked,
                otherUser != null ? com.connectly.dto.response.UserSummaryResponse.fromEntity(otherUser) : null
        );
    }
}
