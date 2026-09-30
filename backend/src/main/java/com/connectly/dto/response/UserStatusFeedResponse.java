package com.connectly.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserStatusFeedResponse {
    private UserSummaryResponse user;
    private List<StatusResponse> statuses = new ArrayList<>();
    private boolean allViewed;
    private LocalDateTime latestStatusCreatedAt;

    public UserStatusFeedResponse() {}

    public UserStatusFeedResponse(UserSummaryResponse user, List<StatusResponse> statuses, boolean allViewed, LocalDateTime latestStatusCreatedAt) {
        this.user = user;
        this.statuses = statuses != null ? statuses : new ArrayList<>();
        this.allViewed = allViewed;
        this.latestStatusCreatedAt = latestStatusCreatedAt;
    }

    public UserSummaryResponse getUser() { return user; }
    public void setUser(UserSummaryResponse user) { this.user = user; }

    public List<StatusResponse> getStatuses() { return statuses; }
    public void setStatuses(List<StatusResponse> statuses) { this.statuses = statuses; }

    public boolean isAllViewed() { return allViewed; }
    public void setAllViewed(boolean allViewed) { this.allViewed = allViewed; }

    public LocalDateTime getLatestStatusCreatedAt() { return latestStatusCreatedAt; }
    public void setLatestStatusCreatedAt(LocalDateTime latestStatusCreatedAt) { this.latestStatusCreatedAt = latestStatusCreatedAt; }
}
