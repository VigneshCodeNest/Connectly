package com.connectly.dto.websocket;

import java.time.LocalDateTime;

public class WsEvent<T> {
    private String type; // e.g., NEW_MESSAGE, MESSAGE_DELIVERED, MESSAGE_READ, USER_PRESENCE, TYPING
    private T data;
    private LocalDateTime timestamp;

    public WsEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public WsEvent(String type, T data) {
        this.type = type;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
