package com.connectly.dto.request;

import jakarta.validation.constraints.NotNull;

public class SendConnectionRequest {

    @NotNull(message = "Receiver user ID is required")
    private Long receiverId;

    public SendConnectionRequest() {}

    public SendConnectionRequest(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
}
