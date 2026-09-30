package com.connectly.service;

import com.connectly.dto.request.SendConnectionRequest;
import com.connectly.dto.response.ConnectionResponse;
import com.connectly.dto.response.ConnectionStatusResponse;
import com.connectly.entity.User;

import java.util.List;

public interface ConnectionService {
    ConnectionResponse sendRequest(SendConnectionRequest request, User currentUser);
    ConnectionResponse sendRequestByEmail(String email, User currentUser);
    List<ConnectionResponse> getIncomingRequests(User currentUser);
    List<ConnectionResponse> getOutgoingRequests(User currentUser);
    ConnectionResponse acceptRequest(Long requestId, User currentUser);
    ConnectionResponse rejectRequest(Long requestId, User currentUser);
    List<ConnectionResponse> getConnectedFriends(User currentUser);
    void removeConnection(Long requestId, User currentUser);
    ConnectionStatusResponse getConnectionStatus(Long targetUserId, User currentUser);
    boolean areUsersConnected(Long user1Id, Long user2Id);
}
