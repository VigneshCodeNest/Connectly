package com.connectly.entity;

import com.connectly.constant.ChatType;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "chats")
public class Chat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatType type = ChatType.DIRECT;

    @Column(name = "name", length = 120)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    public Chat() {}

    public Chat(ChatType type, String name) {
        this.type = type;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChatType getType() { return type; }
    public void setType(ChatType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chat chat)) return false;
        return Objects.equals(id, chat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
