package com.connectly.service;

import com.connectly.dto.request.CreateStatusRequest;
import com.connectly.dto.response.StatusResponse;
import com.connectly.dto.response.StatusViewResponse;
import com.connectly.dto.response.UserStatusFeedResponse;
import com.connectly.entity.User;

import java.util.List;

public interface StatusService {
    StatusResponse createStatus(CreateStatusRequest request, User currentUser);
    List<UserStatusFeedResponse> getStatusFeed(User currentUser);
    List<StatusResponse> getMyStatuses(User currentUser);
    StatusResponse getStatusById(Long statusId, User currentUser);
    StatusResponse viewStatus(Long statusId, User currentUser);
    List<StatusViewResponse> getStatusViewers(Long statusId, User currentUser);
    void deleteStatus(Long statusId, User currentUser);
    int purgeExpiredStatuses();
}
