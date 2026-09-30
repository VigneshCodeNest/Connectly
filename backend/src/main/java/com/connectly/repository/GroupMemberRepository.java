package com.connectly.repository;

import com.connectly.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByUserId(Long userId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.chat.id = :chatId")
    List<GroupMember> findByChatId(@Param("chatId") Long chatId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.chat.id = :chatId AND gm.user.id = :userId")
    Optional<GroupMember> findByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.chat.id = :chatId AND gm.user.id = :userId")
    boolean existsByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
