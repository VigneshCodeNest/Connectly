package com.connectly.dto.websocket;

import com.connectly.constant.MessageStatus;
import java.time.LocalDateTime;

public class WsStatusUpdatePayload {
    private Long chatId;
    private Long messageId; // null if applies to all messages in the chat
    private MessageStatus status; // DELIVERED or READ
    private Long updatedByUserId;
    private LocalDateTime timestamp;

    public WsStatusUpdatePayload() {
        this.timestamp = LocalDateTime.now();
    }

    public WsStatusUpdatePayload(Long chatId, Long messageId, MessageStatus status, Long updatedByUserId) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.status = status;
        this.updatedByUserId = updatedByUserId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public Long getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Long updatedByUserId) { this.updatedByUserId = updatedByUserId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
