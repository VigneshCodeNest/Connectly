package com.connectly.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "chat_participants", uniqueConstraints = {
    @UniqueConstraint(name = "uk_chat_user", columnNames = {"chat_id", "user_id"})
})
public class ChatParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_muted", nullable = false)
    private boolean muted = false;

    @Column(name = "muted_until")
    private java.time.LocalDateTime mutedUntil;

    public ChatParticipant() {}

    public ChatParticipant(Chat chat, User user) {
        this.chat = chat;
        this.user = user;
    }

    public boolean isCurrentlyMuted() {
        if (!muted) {
            return false;
        }
        return mutedUntil == null || mutedUntil.isAfter(java.time.LocalDateTime.now());
    }

    public boolean isMuted() { return muted; }
    public void setMuted(boolean muted) { this.muted = muted; }

    public java.time.LocalDateTime getMutedUntil() { return mutedUntil; }
    public void setMutedUntil(java.time.LocalDateTime mutedUntil) { this.mutedUntil = mutedUntil; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatParticipant that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
