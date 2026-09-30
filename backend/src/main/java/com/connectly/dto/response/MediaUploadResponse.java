package com.connectly.dto.response;

import com.connectly.constant.MessageType;
import java.time.LocalDateTime;

public class MediaUploadResponse {
    private String fileUrl;
    private String fileName;
    private String storedFileName;
    private String fileType;
    private Long fileSize;
    private MessageType mediaType;
    private String category;
    private LocalDateTime uploadedAt;

    public MediaUploadResponse() {
        this.uploadedAt = LocalDateTime.now();
    }

    public MediaUploadResponse(String fileUrl, String fileName, String storedFileName, String fileType, Long fileSize, MessageType mediaType, String category) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.category = category;
        this.uploadedAt = LocalDateTime.now();
    }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public MessageType getMediaType() { return mediaType; }
    public void setMediaType(MessageType mediaType) { this.mediaType = mediaType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
