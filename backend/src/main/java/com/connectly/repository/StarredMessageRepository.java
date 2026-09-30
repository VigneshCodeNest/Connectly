package com.connectly.repository;

import com.connectly.entity.StarredMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StarredMessageRepository extends JpaRepository<StarredMessage, Long> {
    List<StarredMessage> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<StarredMessage> findByUserIdAndMessageId(Long userId, Long messageId);
    boolean existsByUserIdAndMessageId(Long userId, Long messageId);
    void deleteByUserIdAndMessageId(Long userId, Long messageId);
}
