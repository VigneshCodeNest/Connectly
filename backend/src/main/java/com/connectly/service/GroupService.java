package com.connectly.service;

import com.connectly.constant.GroupRole;
import com.connectly.dto.request.AddGroupMembersRequest;
import com.connectly.dto.request.CreateGroupRequest;
import com.connectly.dto.request.UpdateGroupRequest;
import com.connectly.dto.response.GroupMemberResponse;
import com.connectly.dto.response.GroupResponse;
import com.connectly.entity.User;

import java.util.List;

public interface GroupService {
    GroupResponse createGroup(CreateGroupRequest request, User currentUser);
    List<GroupResponse> getUserGroups(User currentUser);
    GroupResponse getGroupDetails(Long groupId, User currentUser);
    GroupResponse getGroupByChatId(Long chatId, User currentUser);
    GroupResponse updateGroupInfo(Long groupId, UpdateGroupRequest request, User currentUser);
    List<GroupMemberResponse> getGroupMembers(Long groupId, User currentUser);
    List<GroupMemberResponse> addMembers(Long groupId, AddGroupMembersRequest request, User currentUser);
    void removeMember(Long groupId, Long userIdToRemove, User currentUser);
    void changeMemberRole(Long groupId, Long targetUserId, GroupRole newRole, User currentUser);
    void leaveGroup(Long groupId, User currentUser);
}
