package com.connectly.dto.request;

import com.connectly.constant.StatusType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateStatusRequest {

    @NotNull(message = "Status type is required (TEXT or IMAGE)")
    private StatusType statusType = StatusType.TEXT;

    @Size(max = 1000, message = "Status content must not exceed 1000 characters")
    private String content;

    private String mediaUrl;

    @Size(max = 30, message = "Background color code must not exceed 30 characters")
    private String backgroundColor;

    public CreateStatusRequest() {}

    public CreateStatusRequest(StatusType statusType, String content) {
        this.statusType = statusType;
        this.content = content;
    }

    public CreateStatusRequest(StatusType statusType, String content, String mediaUrl) {
        this.statusType = statusType;
        this.content = content;
        this.mediaUrl = mediaUrl;
    }

    public StatusType getStatusType() { return statusType; }
    public void setStatusType(StatusType statusType) { this.statusType = statusType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }
}
