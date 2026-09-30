package com.connectly.dto.response;

import com.connectly.entity.MessageAttachment;

public class MessageAttachmentResponse {
    private Long id;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public MessageAttachmentResponse() {}

    public static MessageAttachmentResponse fromEntity(MessageAttachment att) {
        if (att == null) return null;
        MessageAttachmentResponse res = new MessageAttachmentResponse();
        res.setId(att.getId());
        res.setFileUrl(att.getFileUrl());
        res.setFileName(att.getFileName());
        res.setFileType(att.getFileType());
        res.setFileSize(att.getFileSize());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
