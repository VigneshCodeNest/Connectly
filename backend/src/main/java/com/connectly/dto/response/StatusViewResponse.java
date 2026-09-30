package com.connectly.dto.response;

import com.connectly.entity.StatusView;
import java.time.LocalDateTime;

public class StatusViewResponse {
    private UserSummaryResponse viewer;
    private LocalDateTime viewedAt;

    public StatusViewResponse() {}

    public StatusViewResponse(UserSummaryResponse viewer, LocalDateTime viewedAt) {
        this.viewer = viewer;
        this.viewedAt = viewedAt;
    }

    public static StatusViewResponse fromEntity(StatusView sv) {
        if (sv == null) return null;
        return new StatusViewResponse(UserSummaryResponse.fromEntity(sv.getViewer()), sv.getViewedAt());
    }

    public UserSummaryResponse getViewer() { return viewer; }
    public void setViewer(UserSummaryResponse viewer) { this.viewer = viewer; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
