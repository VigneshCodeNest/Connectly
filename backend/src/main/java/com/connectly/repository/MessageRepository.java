package com.connectly.repository;

import com.connectly.constant.MessageStatus;
import com.connectly.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(Long chatId, Pageable pageable);
    List<Message> findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(Long chatId);
    Optional<Message> findTopByChatIdAndIsDeletedFalseOrderByCreatedAtDesc(Long chatId);
    long countByChatIdAndSenderIdNotAndStatusNot(Long chatId, Long senderId, MessageStatus status);
    List<Message> findByChatIdAndSenderIdNotAndStatusNot(Long chatId, Long senderId, MessageStatus status);
    List<Message> findByChatIdAndSenderIdNotAndStatus(Long chatId, Long senderId, MessageStatus status);

    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.status != :newStatus")
    int updateStatusForReceivedMessages(@Param("chatId") Long chatId, @Param("userId") Long userId, @Param("newStatus") MessageStatus newStatus);

    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus WHERE m.id = :messageId AND m.chat.id = :chatId AND m.sender.id != :userId")
    int updateSingleMessageStatus(@Param("chatId") Long chatId, @Param("messageId") Long messageId, @Param("userId") Long userId, @Param("newStatus") MessageStatus newStatus);
}
