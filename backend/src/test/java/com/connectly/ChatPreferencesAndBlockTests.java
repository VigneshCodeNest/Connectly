package com.connectly;

import com.connectly.constant.ConnectionStatus;
import com.connectly.constant.MuteOption;
import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.MuteChatRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ChatResponse;
import com.connectly.entity.ConnectionRequest;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatPreferencesAndBlockTests {

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
    private BlockedUserRepository blockedUserRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private StatusViewRepository statusViewRepository;

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
    private String tokenA;
    private String tokenB;
    private String tokenC;
    private Long directChatId;

    @BeforeEach
    void setUp() {
        statusViewRepository.deleteAll();
        statusRepository.deleteAll();
        blockedUserRepository.deleteAll();
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

        // Connect User A and User B
        ConnectionRequest connAB = new ConnectionRequest(userA, userB, ConnectionStatus.ACCEPTED);
        connectionRepository.save(connAB);

        // Direct Chat between User A and User B
        ChatResponse chatRes = chatService.getOrCreateDirectChat(new CreateDirectChatRequest(userB.getId()), userA);
        directChatId = chatRes.getId();
    }

    @Test
    @DisplayName("Mute Chat: User A mutes chat for 1 hour, verify User B is NOT muted")
    void testMuteChat_DurationOptions_AndPerUserIsolation() throws Exception {
        // 1. User A mutes chat for 1 hour
        MuteChatRequest mute1h = new MuteChatRequest(MuteOption.ONE_HOUR);
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/mute")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mute1h)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isMuted", is(true)))
                .andExpect(jsonPath("$.data.mutedUntil", notNullValue()));

        // 2. Verify User A's chat list reflects isMuted = true
        mockMvc.perform(get("/api/v1/chats")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isMuted", is(true)));

        // 3. CRITICAL ISOLATION RULE: User B's chat list reflects isMuted = false
        mockMvc.perform(get("/api/v1/chats")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isMuted", is(false)))
                .andExpect(jsonPath("$.data[0].mutedUntil", nullValue()));
    }

    @Test
    @DisplayName("Mute Chat: Support 8 hours, 1 week, and indefinite mute options")
    void testMuteChat_AllOptions() throws Exception {
        // 8 Hours
        MuteChatRequest mute8h = new MuteChatRequest(MuteOption.EIGHT_HOURS);
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/mute")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mute8h)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isMuted", is(true)));

        // 1 Week
        MuteChatRequest muteWeek = new MuteChatRequest(MuteOption.ONE_WEEK);
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/mute")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(muteWeek)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isMuted", is(true)));

        // Indefinitely / until manually unmuted
        MuteChatRequest muteIndefinite = new MuteChatRequest(MuteOption.UNTIL_MANUALLY_UNMUTED);
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/mute")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(muteIndefinite)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isMuted", is(true)))
                .andExpect(jsonPath("$.data.mutedUntil", nullValue()));
    }

    @Test
    @DisplayName("Unmute Chat: Successfully unmute chat notifications")
    void testUnmuteChat_Success() throws Exception {
        // Mute first
        chatService.muteChat(directChatId, new MuteChatRequest(MuteOption.ONE_HOUR), userA);

        // Unmute via POST /unmute
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/unmute")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isMuted", is(false)))
                .andExpect(jsonPath("$.data.mutedUntil", nullValue()));

        // Mute again and unmute via DELETE /mute
        chatService.muteChat(directChatId, new MuteChatRequest(MuteOption.ONE_HOUR), userA);
        mockMvc.perform(delete("/api/v1/chats/" + directChatId + "/mute")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isMuted", is(false)));
    }

    @Test
    @DisplayName("Star / Unstar Message: Star a message and view in starred list")
    void testStarAndUnstarMessage() throws Exception {
        // User A sends message
        SendMessageRequest sendReq = new SendMessageRequest("Important reference document");
        var msgRes = chatService.sendMessage(directChatId, sendReq, userA);

        // User B stars User A's message
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/messages/" + msgRes.getId() + "/star")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        // User B retrieves starred messages
        mockMvc.perform(get("/api/v1/chats/starred")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(msgRes.getId().intValue())))
                .andExpect(jsonPath("$.data[0].starred", is(true)));

        // User B unstars message
        mockMvc.perform(delete("/api/v1/chats/" + directChatId + "/messages/" + msgRes.getId() + "/star")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        // Verify starred messages list is now empty
        mockMvc.perform(get("/api/v1/chats/starred")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Block & Unblock User: Block lifecycle and block status inspection")
    void testBlockAndUnblockUser_Lifecycle() throws Exception {
        // 1. User A blocks User B
        mockMvc.perform(post("/api/v1/users/" + userB.getId() + "/block")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedUser.username", is("bob_v")));

        // 2. User A retrieves blocked users list
        mockMvc.perform(get("/api/v1/users/blocked")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].blockedUser.username", is("bob_v")));

        // 3. User A checks block status with User B
        mockMvc.perform(get("/api/v1/users/" + userB.getId() + "/block-status")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBlocked", is(true)))
                .andExpect(jsonPath("$.data.blockedByMe", is(true)))
                .andExpect(jsonPath("$.data.blockedByThem", is(false)));

        // 4. User B checks block status with User A
        mockMvc.perform(get("/api/v1/users/" + userA.getId() + "/block-status")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isBlocked", is(true)))
                .andExpect(jsonPath("$.data.blockedByMe", is(false)))
                .andExpect(jsonPath("$.data.blockedByThem", is(true)));

        // 5. User A unblocks User B
        mockMvc.perform(delete("/api/v1/users/" + userB.getId() + "/block")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        // 6. Blocked list is empty
        mockMvc.perform(get("/api/v1/users/blocked")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("Block Enforcement: Blocked users cannot send messages or connection requests")
    void testBlockEnforcement_CommunicationBlocked() throws Exception {
        // User A blocks User B
        mockMvc.perform(post("/api/v1/users/" + userB.getId() + "/block")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        // 1. User B tries to send message to User A -> REJECTED
        SendMessageRequest req = new SendMessageRequest("Hello Alice, are you there?");
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/messages")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("blocked")));

        // 2. User A tries to send message to User B -> REJECTED
        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("blocked")));

        // 3. User A unblocks User B -> Communication resumes
        mockMvc.perform(delete("/api/v1/users/" + userB.getId() + "/block")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/chats/" + directChatId + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Self Block Prevention: User cannot block themselves")
    void testSelfBlock_Rejected() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + userA.getId() + "/block")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cannot block yourself")));
    }
}
