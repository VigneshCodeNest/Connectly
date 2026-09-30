package com.connectly.dto.request;

import com.connectly.constant.MessageType;

public class SendMessageRequest {

    private String content;
    private MessageType messageType = MessageType.TEXT;
    private Long replyToId;
    private boolean isForwarded = false;

    // Optional attachment metadata
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public SendMessageRequest() {}

    public SendMessageRequest(String content) {
        this.content = content;
        this.messageType = MessageType.TEXT;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public boolean isForwarded() { return isForwarded; }
    public void setForwarded(boolean forwarded) { isForwarded = forwarded; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
