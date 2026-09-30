package com.connectly;

import com.connectly.constant.*;
import com.connectly.entity.*;
import com.connectly.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class EntityMappingTests {

    @Autowired private UserRepository userRepository;
    @Autowired private ChatRepository chatRepository;
    @Autowired private ChatParticipantRepository chatParticipantRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private ConnectionRequestRepository connectionRequestRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private StarredMessageRepository starredMessageRepository;
    @Autowired private BlockedUserRepository blockedUserRepository;

    @Test
    void testUserAndConnectionMapping() {
        User arun = new User("Arun Kumar", "arun@example.com", "@arun", "hashed_pass_123");
        User priya = new User("Priya", "priya@example.com", "@priya", "hashed_pass_456");
        userRepository.save(arun);
        userRepository.save(priya);

        assertNotNull(arun.getId());
        assertNotNull(priya.getId());

        ConnectionRequest request = new ConnectionRequest(arun, priya, ConnectionStatus.ACCEPTED);
        connectionRequestRepository.save(request);

        assertTrue(connectionRequestRepository.areConnected(arun.getId(), priya.getId()));
    }

    @Test
    void testChatAndMessageMapping() {
        User sender = userRepository.save(new User("Sender", "sender@example.com", "@sender", "pass"));
        Chat chat = chatRepository.save(new Chat(ChatType.DIRECT, "Direct Chat"));

        chatParticipantRepository.save(new ChatParticipant(chat, sender));

        Message msg = new Message();
        msg.setChat(chat);
        msg.setSender(sender);
        msg.setContent("Hello World");
        msg.setMessageType(MessageType.TEXT);
        msg.setStatus(MessageStatus.SENT);
        messageRepository.save(msg);

        assertNotNull(msg.getId());
        assertEquals("Hello World", messageRepository.findById(msg.getId()).orElseThrow().getContent());
    }

    @Test
    void testStatusAndExpiration() {
        User user = userRepository.save(new User("StatusUser", "status@example.com", "@statususer", "pass"));
        Status status = new Status();
        status.setUser(user);
        status.setStatusType(StatusType.TEXT);
        status.setContent("Testing my status");
        status.setExpiresAt(LocalDateTime.now().plusHours(24));
        statusRepository.save(status);

        assertEquals(1, statusRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(user.getId(), LocalDateTime.now()).size());
    }
}
