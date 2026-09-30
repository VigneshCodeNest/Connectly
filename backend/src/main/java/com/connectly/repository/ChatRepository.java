package com.connectly.repository;

import com.connectly.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT cp1.chat FROM ChatParticipant cp1 " +
           "JOIN ChatParticipant cp2 ON cp1.chat.id = cp2.chat.id " +
           "WHERE cp1.chat.type = 'DIRECT' AND cp1.user.id = :user1 AND cp2.user.id = :user2")
    Optional<Chat> findDirectChatBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);
}
