package com.connectly;

import com.connectly.constant.ConnectionStatus;
import com.connectly.constant.MessageType;
import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.ForwardMessageRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ChatResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.entity.ConnectionRequest;
import com.connectly.entity.Message;
import com.connectly.entity.User;
import com.connectly.repository.*;
import com.connectly.security.JwtTokenProvider;
import com.connectly.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MessageFeaturesTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRequestRepository connectionRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatParticipantRepository participantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageAttachmentRepository attachmentRepository;

    @Autowired
    private StarredMessageRepository starredMessageRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ChatService chatService;

    private User userA;
    private User userB;
    private User userC;
    private User userD; // Unconnected user
    private String tokenA;
    private String tokenB;
    private String tokenC;
    private ChatResponse chatAB;
    private ChatResponse chatAC;

    @BeforeEach
    void setUp() {
        starredMessageRepository.deleteAll();
        attachmentRepository.deleteAll();
        messageRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        participantRepository.deleteAll();
        chatRepository.deleteAll();
        connectionRepository.deleteAll();
        userRepository.deleteAll();

        // User A
        userA = new User("Alice Walker", "alice@connectly.io", "alice_w", passwordEncoder.encode("Pass@1234"));
        userA = userRepository.save(userA);
        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail());

        // User B
        userB = new User("Bob Vance", "bob@connectly.io", "bob_v", passwordEncoder.encode("Pass@1234"));
        userB = userRepository.save(userB);
        tokenB = jwtTokenProvider.generateToken(userB.getId(), userB.getEmail());

        // User C
        userC = new User("Charlie Brown", "charlie@connectly.io", "charlie_b", passwordEncoder.encode("Pass@1234"));
        userC = userRepository.save(userC);
        tokenC = jwtTokenProvider.generateToken(userC.getId(), userC.getEmail());

        // User D (not connected)
        userD = new User("David Miller", "david@connectly.io", "david_m", passwordEncoder.encode("Pass@1234"));
        userD = userRepository.save(userD);

        // Connect A & B
        ConnectionRequest connAB = new ConnectionRequest(userA, userB, ConnectionStatus.ACCEPTED);
        connectionRepository.save(connAB);
        CreateDirectChatRequest reqAB = new CreateDirectChatRequest();
        reqAB.setTargetUserId(userB.getId());
        chatAB = chatService.getOrCreateDirectChat(reqAB, userA);

        // Connect A & C
        ConnectionRequest connAC = new ConnectionRequest(userA, userC, ConnectionStatus.ACCEPTED);
        connectionRepository.save(connAC);
        CreateDirectChatRequest reqAC = new CreateDirectChatRequest();
        reqAC.setTargetUserId(userC.getId());
        chatAC = chatService.getOrCreateDirectChat(reqAC, userA);
    }

    @Test
    @DisplayName("Reply to Message: Successfully reply to another message in same conversation")
    void testReplyToMessage_Success() throws Exception {
        Long chatId = chatAB.getId();

        // 1. User A sends first message
        SendMessageRequest msg1Req = new SendMessageRequest("What time is the meeting?");
        MessageResponse msg1 = chatService.sendMessage(chatId, msg1Req, userA);

        // 2. User B replies to msg1
        SendMessageRequest replyReq = new SendMessageRequest("It's at 3 PM today.");
        replyReq.setReplyToId(msg1.getId());

        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.replyToId", is(msg1.getId().intValue())))
                .andExpect(jsonPath("$.data.replyToContent", is("What time is the meeting?")))
                .andExpect(jsonPath("$.data.replyToSenderName", is("Alice Walker")));

        // Verify persistence
        List<Message> messages = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatId);
        assertEquals(2, messages.size());
        Message replyMsg = messages.get(1);
        assertNotNull(replyMsg.getReplyTo());
        assertEquals(msg1.getId(), replyMsg.getReplyTo().getId());
    }

    @Test
    @DisplayName("Reply to Message: Cannot reply to a message from a different conversation")
    void testReplyToMessage_CrossChat_Forbidden() throws Exception {
        // User A sends message in chatAB
        SendMessageRequest msg1Req = new SendMessageRequest("Secret in chat AB");
        MessageResponse msgAB = chatService.sendMessage(chatAB.getId(), msg1Req, userA);

        // User A attempts to reply to msgAB inside chatAC
        SendMessageRequest invalidReply = new SendMessageRequest("Trying cross-chat reply");
        invalidReply.setReplyToId(msgAB.getId());

        mockMvc.perform(post("/api/v1/chats/" + chatAC.getId() + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReply)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("different conversation")));
    }

    @Test
    @DisplayName("Forward Message: Successfully forward message to another connected conversation")
    void testForwardMessage_Success() throws Exception {
        // User B sends message to User A in chatAB
        SendMessageRequest origReq = new SendMessageRequest("Important announcement!");
        MessageResponse origMsg = chatService.sendMessage(chatAB.getId(), origReq, userB);

        // User A forwards message to chatAC
        ForwardMessageRequest forwardReq = new ForwardMessageRequest();
        forwardReq.setTargetChatIds(Collections.singletonList(chatAC.getId()));

        mockMvc.perform(post("/api/v1/chats/" + chatAB.getId() + "/messages/" + origMsg.getId() + "/forward")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forwardReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].content", is("Important announcement!")))
                .andExpect(jsonPath("$.data[0].forwarded", is(true)))
                .andExpect(jsonPath("$.data[0].chatId", is(chatAC.getId().intValue())));

        // Verify message exists in chatAC with isForwarded = true
        List<Message> acMessages = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatAC.getId());
        assertEquals(1, acMessages.size());
        assertTrue(acMessages.get(0).isForwarded());
        assertEquals(userA.getId(), acMessages.get(0).getSender().getId());
    }

    @Test
    @DisplayName("Forward Message: Forwarding to a non-connected user is REJECTED")
    void testForwardMessage_ToNonConnectedUser_Forbidden() throws Exception {
        SendMessageRequest origReq = new SendMessageRequest("Classified info");
        MessageResponse origMsg = chatService.sendMessage(chatAB.getId(), origReq, userA);

        // User A attempts to forward to User D (not connected)
        ForwardMessageRequest forwardReq = new ForwardMessageRequest();
        forwardReq.setTargetUserIds(Collections.singletonList(userD.getId()));

        mockMvc.perform(post("/api/v1/chats/" + chatAB.getId() + "/messages/" + origMsg.getId() + "/forward")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forwardReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not connected")));
    }

    @Test
    @DisplayName("Delete Message: Sender can soft-delete their own message")
    void testDeleteMessage_Sender_Success() throws Exception {
        SendMessageRequest origReq = new SendMessageRequest("Typo to delete");
        MessageResponse origMsg = chatService.sendMessage(chatAB.getId(), origReq, userA);

        mockMvc.perform(delete("/api/v1/chats/" + chatAB.getId() + "/messages/" + origMsg.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify message is soft-deleted in MySQL
        Message msgInDb = messageRepository.findById(origMsg.getId()).orElseThrow();
        assertTrue(msgInDb.isDeleted());
    }

    @Test
    @DisplayName("Delete Message: User cannot delete another user's message (Forbidden)")
    void testDeleteMessage_NonSender_Forbidden() throws Exception {
        // User A sends message
        SendMessageRequest origReq = new SendMessageRequest("Alice's message");
        MessageResponse origMsg = chatService.sendMessage(chatAB.getId(), origReq, userA);

        // User B attempts to delete Alice's message
        mockMvc.perform(delete("/api/v1/chats/" + chatAB.getId() + "/messages/" + origMsg.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("not authorized")));

        // Verify message is NOT deleted
        Message msgInDb = messageRepository.findById(origMsg.getId()).orElseThrow();
        assertFalse(msgInDb.isDeleted());
    }

    @Test
    @DisplayName("Star / Unstar Messages: Star message, retrieve starred list, and unstar")
    void testStarAndUnstarMessages() throws Exception {
        SendMessageRequest origReq = new SendMessageRequest("Remember this recipe!");
        MessageResponse msg = chatService.sendMessage(chatAB.getId(), origReq, userA);

        // 1. User B stars Alice's message
        mockMvc.perform(post("/api/v1/chats/" + chatAB.getId() + "/messages/" + msg.getId() + "/star")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 2. User B retrieves starred messages
        mockMvc.perform(get("/api/v1/messages/starred")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(msg.getId().intValue())))
                .andExpect(jsonPath("$.data[0].starred", is(true)))
                .andExpect(jsonPath("$.data[0].content", is("Remember this recipe!")));

        // 3. User B unstars the message
        mockMvc.perform(delete("/api/v1/chats/" + chatAB.getId() + "/messages/" + msg.getId() + "/star")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. Starred list is now empty
        mockMvc.perform(get("/api/v1/messages/starred")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Star Message: Non-participant cannot star messages from private conversations")
    void testStarMessage_NonParticipant_Forbidden() throws Exception {
        SendMessageRequest origReq = new SendMessageRequest("Private chat between A & B");
        MessageResponse msg = chatService.sendMessage(chatAB.getId(), origReq, userA);

        // User C (not in chatAB) attempts to star message
        mockMvc.perform(post("/api/v1/chats/" + chatAB.getId() + "/messages/" + msg.getId() + "/star")
                        .header("Authorization", "Bearer " + tokenC))
                .andExpect(status().isUnauthorized());
    }
}
