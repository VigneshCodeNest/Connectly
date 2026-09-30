package com.connectly.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_username", columnList = "username", unique = true)
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "username", nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "bio", length = 300)
    private String bio;

    @Column(name = "color_accent", length = 30)
    private String colorAccent;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;

    @Column(name = "last_seen_privacy", nullable = false, length = 30)
    private String lastSeenPrivacy = "Everyone";

    @Column(name = "profile_photo_privacy", nullable = false, length = 30)
    private String profilePhotoPrivacy = "Everyone";

    public User() {}

    public User(String name, String email, String username, String passwordHash) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getColorAccent() { return colorAccent; }
    public void setColorAccent(String colorAccent) { this.colorAccent = colorAccent; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getLastSeenPrivacy() { return lastSeenPrivacy; }
    public void setLastSeenPrivacy(String lastSeenPrivacy) { this.lastSeenPrivacy = lastSeenPrivacy; }

    public String getProfilePhotoPrivacy() { return profilePhotoPrivacy; }
    public void setProfilePhotoPrivacy(String profilePhotoPrivacy) { this.profilePhotoPrivacy = profilePhotoPrivacy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
