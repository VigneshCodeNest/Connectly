package com.connectly.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ChatPreferencesResponse {

    private Long chatId;

    @JsonProperty("isMuted")
    private boolean isMuted;

    private LocalDateTime mutedUntil;

    @JsonProperty("isBlocked")
    private boolean isBlocked;

    private Long otherUserId;
    private UserSummaryResponse otherUser;

    public ChatPreferencesResponse() {}

    public ChatPreferencesResponse(Long chatId, boolean isMuted, LocalDateTime mutedUntil, boolean isBlocked, UserSummaryResponse otherUser) {
        this.chatId = chatId;
        this.isMuted = isMuted;
        this.mutedUntil = mutedUntil;
        this.isBlocked = isBlocked;
        this.otherUser = otherUser;
        if (otherUser != null) {
            this.otherUserId = otherUser.getId();
        }
    }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }

    public LocalDateTime getMutedUntil() { return mutedUntil; }
    public void setMutedUntil(LocalDateTime mutedUntil) { this.mutedUntil = mutedUntil; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public UserSummaryResponse getOtherUser() { return otherUser; }
    public void setOtherUser(UserSummaryResponse otherUser) { this.otherUser = otherUser; }
}
