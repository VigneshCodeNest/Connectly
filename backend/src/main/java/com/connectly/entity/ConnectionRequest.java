package com.connectly.entity;

import com.connectly.constant.ConnectionStatus;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "connection_requests", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sender_receiver", columnNames = {"sender_id", "receiver_id"})
}, indexes = {
    @Index(name = "idx_conn_sender", columnList = "sender_id"),
    @Index(name = "idx_conn_receiver", columnList = "receiver_id")
})
public class ConnectionRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ConnectionStatus status = ConnectionStatus.PENDING;

    public ConnectionRequest() {}

    public ConnectionRequest(User sender, User receiver, ConnectionStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public ConnectionStatus getStatus() { return status; }
    public void setStatus(ConnectionStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionRequest that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
