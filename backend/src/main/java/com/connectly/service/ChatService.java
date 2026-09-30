package com.connectly.service;

import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.ForwardMessageRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ChatResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.response.UserSummaryResponse;
import com.connectly.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ChatResponse getOrCreateDirectChat(CreateDirectChatRequest request, User currentUser);
    List<ChatResponse> getUserChats(User currentUser);
    ChatResponse getChatDetails(Long chatId, User currentUser);
    List<UserSummaryResponse> getChatParticipants(Long chatId, User currentUser);
    Page<MessageResponse> getChatMessages(Long chatId, Pageable pageable, User currentUser);
    MessageResponse sendMessage(Long chatId, SendMessageRequest request, User currentUser);
    void markChatMessagesAsRead(Long chatId, User currentUser);
    void markMessageAsDelivered(Long chatId, Long messageId, User currentUser);
    void markAllChatMessagesAsDelivered(Long chatId, User currentUser);

    // Phase 8: Advanced Message Features
    List<MessageResponse> forwardMessage(Long sourceChatId, Long messageId, ForwardMessageRequest request, User currentUser);
    void deleteMessage(Long chatId, Long messageId, User currentUser);
    void starMessage(Long chatId, Long messageId, User currentUser);
    void unstarMessage(Long chatId, Long messageId, User currentUser);
    List<MessageResponse> getStarredMessages(User currentUser);

    // Phase 11: Chat Preferences & Muting Notifications
    com.connectly.dto.response.ChatPreferencesResponse muteChat(Long chatId, com.connectly.dto.request.MuteChatRequest request, User currentUser);
    com.connectly.dto.response.ChatPreferencesResponse unmuteChat(Long chatId, User currentUser);
    com.connectly.dto.response.ChatPreferencesResponse getChatPreferences(Long chatId, User currentUser);
}
