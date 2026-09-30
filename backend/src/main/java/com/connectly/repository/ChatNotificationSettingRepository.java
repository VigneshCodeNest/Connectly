package com.connectly.repository;

import com.connectly.entity.ChatNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatNotificationSettingRepository extends JpaRepository<ChatNotificationSetting, Long> {
    Optional<ChatNotificationSetting> findByUserIdAndChatId(Long userId, Long chatId);
}
