package com.connectly.dto.response;

import com.connectly.entity.Group;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupResponse {
    private Long id;
    private Long chatId;
    private String name;
    private String description;
    private String avatarUrl;
    private UserSummaryResponse createdBy;
    private List<GroupMemberResponse> members = new ArrayList<>();
    private int memberCount;
    private boolean isAdmin;
    private MessageResponse lastMessage;
    private long unreadCount;
    private LocalDateTime createdAt;

    public GroupResponse() {}

    public static GroupResponse fromEntity(Group group, List<GroupMemberResponse> members, boolean isAdmin, MessageResponse lastMessage, long unreadCount) {
        if (group == null) return null;
        GroupResponse res = new GroupResponse();
        res.setId(group.getId());
        res.setChatId(group.getChat() != null ? group.getChat().getId() : null);
        res.setName(group.getName());
        res.setDescription(group.getDescription());
        res.setAvatarUrl(group.getChat() != null ? group.getChat().getAvatarUrl() : null);
        res.setCreatedBy(UserSummaryResponse.fromEntity(group.getCreatedBy()));
        res.setMembers(members != null ? members : new ArrayList<>());
        res.setMemberCount(members != null ? members.size() : 0);
        res.setAdmin(isAdmin);
        res.setLastMessage(lastMessage);
        res.setUnreadCount(unreadCount);
        res.setCreatedAt(group.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public UserSummaryResponse getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserSummaryResponse createdBy) { this.createdBy = createdBy; }

    public List<GroupMemberResponse> getMembers() { return members; }
    public void setMembers(List<GroupMemberResponse> members) { this.members = members; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public MessageResponse getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageResponse lastMessage) { this.lastMessage = lastMessage; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
