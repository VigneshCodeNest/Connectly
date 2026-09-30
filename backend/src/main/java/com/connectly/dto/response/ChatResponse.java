package com.connectly.dto.response;

import com.connectly.constant.ChatType;
import com.connectly.entity.Chat;
import com.connectly.entity.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ChatResponse {
    private Long id;
    private ChatType type;
    private String name;
    private String description;
    private String avatarUrl;
    private UserSummaryResponse otherUser;
    private MessageResponse lastMessage;
    private long unreadCount = 0;

    @JsonProperty("isMuted")
    private boolean isMuted = false;

    private LocalDateTime mutedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChatResponse() {}

    public static ChatResponse fromEntity(Chat chat, User otherUser, MessageResponse lastMessage, long unreadCount) {
        return fromEntity(chat, otherUser, lastMessage, unreadCount, false, null);
    }

    public static ChatResponse fromEntity(Chat chat, User otherUser, MessageResponse lastMessage, long unreadCount, boolean isMuted, LocalDateTime mutedUntil) {
        if (chat == null) return null;
        ChatResponse res = new ChatResponse();
        res.setId(chat.getId());
        res.setType(chat.getType());
        res.setDescription(chat.getDescription());
        res.setCreatedAt(chat.getCreatedAt());
        res.setUpdatedAt(chat.getUpdatedAt());
        res.setMuted(isMuted);
        res.setMutedUntil(mutedUntil);

        if (chat.getType() == ChatType.DIRECT && otherUser != null) {
            res.setName(otherUser.getName());
            res.setAvatarUrl(otherUser.getAvatarUrl());
            res.setOtherUser(UserSummaryResponse.fromEntity(otherUser));
        } else {
            res.setName(chat.getName());
            res.setAvatarUrl(chat.getAvatarUrl());
        }

        res.setLastMessage(lastMessage);
        res.setUnreadCount(unreadCount);
        return res;
    }

    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }

    public LocalDateTime getMutedUntil() { return mutedUntil; }
    public void setMutedUntil(LocalDateTime mutedUntil) { this.mutedUntil = mutedUntil; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatType getType() { return type; }
    public void setType(ChatType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public UserSummaryResponse getOtherUser() { return otherUser; }
    public void setOtherUser(UserSummaryResponse otherUser) { this.otherUser = otherUser; }

    public MessageResponse getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageResponse lastMessage) { this.lastMessage = lastMessage; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
