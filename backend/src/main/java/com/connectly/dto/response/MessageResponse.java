package com.connectly.dto.response;

import com.connectly.constant.MessageStatus;
import com.connectly.constant.MessageType;
import com.connectly.entity.Message;
import com.connectly.entity.MessageAttachment;

import java.time.LocalDateTime;

public class MessageResponse {
    private Long id;
    private Long chatId;
    private UserSummaryResponse sender;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private Long replyToId;
    private String replyToContent;
    private String replyToSenderName;
    private boolean isForwarded;
    private boolean isDeleted;
    private boolean isStarred;
    private MessageAttachmentResponse attachment;
    private LocalDateTime createdAt;

    public MessageResponse() {}

    public static MessageResponse fromEntity(Message msg, MessageAttachment attachment) {
        return fromEntity(msg, attachment, false);
    }

    public static MessageResponse fromEntity(Message msg, MessageAttachment attachment, boolean isStarred) {
        if (msg == null) return null;
        MessageResponse res = new MessageResponse();
        res.setId(msg.getId());
        res.setChatId(msg.getChat() != null ? msg.getChat().getId() : null);
        res.setSender(UserSummaryResponse.fromEntity(msg.getSender()));
        res.setContent(msg.getContent());
        res.setMessageType(msg.getMessageType());
        res.setStatus(msg.getStatus());
        res.setForwarded(msg.isForwarded());
        res.setDeleted(msg.isDeleted());
        res.setStarred(isStarred);
        res.setCreatedAt(msg.getCreatedAt());

        if (msg.getReplyTo() != null) {
            res.setReplyToId(msg.getReplyTo().getId());
            res.setReplyToContent(msg.getReplyTo().getContent());
            if (msg.getReplyTo().getSender() != null) {
                res.setReplyToSenderName(msg.getReplyTo().getSender().getName());
            }
        }

        if (attachment != null) {
            res.setAttachment(MessageAttachmentResponse.fromEntity(attachment));
        }

        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public UserSummaryResponse getSender() { return sender; }
    public void setSender(UserSummaryResponse sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public String getReplyToContent() { return replyToContent; }
    public void setReplyToContent(String replyToContent) { this.replyToContent = replyToContent; }

    public String getReplyToSenderName() { return replyToSenderName; }
    public void setReplyToSenderName(String replyToSenderName) { this.replyToSenderName = replyToSenderName; }

    public boolean isForwarded() { return isForwarded; }
    public void setForwarded(boolean forwarded) { isForwarded = forwarded; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public boolean isStarred() { return isStarred; }
    public void setStarred(boolean starred) { isStarred = starred; }

    public MessageAttachmentResponse getAttachment() { return attachment; }
    public void setAttachment(MessageAttachmentResponse attachment) { this.attachment = attachment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
