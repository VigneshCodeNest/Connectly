package com.connectly.dto.response;

import com.connectly.constant.ConnectionStatus;
import com.connectly.entity.ConnectionRequest;
import com.connectly.entity.User;

import java.time.LocalDateTime;

public class ConnectionResponse {
    private Long id;
    private ConnectionStatus status;
    private UserSummaryResponse sender;
    private UserSummaryResponse receiver;
    private UserSummaryResponse connectedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ConnectionResponse() {}

    public static ConnectionResponse fromEntity(ConnectionRequest entity, User currentUser) {
        if (entity == null) return null;
        ConnectionResponse res = new ConnectionResponse();
        res.setId(entity.getId());
        res.setStatus(entity.getStatus());
        res.setSender(UserSummaryResponse.fromEntity(entity.getSender()));
        res.setReceiver(UserSummaryResponse.fromEntity(entity.getReceiver()));

        // The other user in the connection
        if (currentUser != null) {
            User other = entity.getSender().getId().equals(currentUser.getId())
                    ? entity.getReceiver()
                    : entity.getSender();
            res.setConnectedUser(UserSummaryResponse.fromEntity(other));
        }

        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus status) { this.status = status; }

    public UserSummaryResponse getSender() { return sender; }
    public void setSender(UserSummaryResponse sender) { this.sender = sender; }

    public UserSummaryResponse getReceiver() { return receiver; }
    public void setReceiver(UserSummaryResponse receiver) { this.receiver = receiver; }

    public UserSummaryResponse getConnectedUser() { return connectedUser; }
    public void setConnectedUser(UserSummaryResponse connectedUser) { this.connectedUser = connectedUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
