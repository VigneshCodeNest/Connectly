package com.connectly.dto.websocket;

public class WsTypingPayload {
    private Long chatId;
    private Long userId;
    private String userName;
    private boolean isTyping;

    public WsTypingPayload() {}

    public WsTypingPayload(Long chatId, Long userId, String userName, boolean isTyping) {
        this.chatId = chatId;
        this.userId = userId;
        this.userName = userName;
        this.isTyping = isTyping;
    }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }
}
