package com.connectly.dto.response;

import com.connectly.constant.StatusType;
import com.connectly.entity.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StatusResponse {
    private Long id;
    private UserSummaryResponse user;
    private StatusType statusType;
    private String content;
    private String mediaUrl;
    private String backgroundColor;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;
    private int viewCount;
    private boolean viewedByCurrentUser;
    private List<StatusViewResponse> viewers = new ArrayList<>();

    public StatusResponse() {}

    public static StatusResponse fromEntity(Status status, int viewCount, boolean viewedByCurrentUser, List<StatusViewResponse> viewers) {
        if (status == null) return null;
        StatusResponse res = new StatusResponse();
        res.setId(status.getId());
        res.setUser(UserSummaryResponse.fromEntity(status.getUser()));
        res.setStatusType(status.getStatusType());
        res.setContent(status.getContent());
        res.setMediaUrl(status.getMediaUrl());
        res.setBackgroundColor(status.getBackgroundColor());
        res.setCreatedAt(status.getCreatedAt());
        res.setExpiresAt(status.getExpiresAt());
        res.setExpired(status.getExpiresAt() != null && status.getExpiresAt().isBefore(LocalDateTime.now()));
        res.setViewCount(viewCount);
        res.setViewedByCurrentUser(viewedByCurrentUser);
        res.setViewers(viewers != null ? viewers : new ArrayList<>());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserSummaryResponse getUser() { return user; }
    public void setUser(UserSummaryResponse user) { this.user = user; }

    public StatusType getStatusType() { return statusType; }
    public void setStatusType(StatusType statusType) { this.statusType = statusType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public boolean isViewedByCurrentUser() { return viewedByCurrentUser; }
    public void setViewedByCurrentUser(boolean viewedByCurrentUser) { this.viewedByCurrentUser = viewedByCurrentUser; }

    public List<StatusViewResponse> getViewers() { return viewers; }
    public void setViewers(List<StatusViewResponse> viewers) { this.viewers = viewers; }
}
