package com.connectly.repository;

import com.connectly.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<ChatParticipant> findByChatId(Long chatId);
    Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);
    boolean existsByChatIdAndUserId(Long chatId, Long userId);
}
