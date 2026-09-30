package com.connectly;

import com.connectly.constant.ConnectionStatus;
import com.connectly.constant.GroupRole;
import com.connectly.constant.MessageType;
import com.connectly.dto.request.AddGroupMembersRequest;
import com.connectly.dto.request.CreateGroupRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.request.UpdateGroupRequest;
import com.connectly.dto.response.GroupResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.websocket.WsChatMessagePayload;
import com.connectly.dto.websocket.WsEvent;
import com.connectly.entity.ConnectionRequest;
import com.connectly.entity.Group;
import com.connectly.entity.Message;
import com.connectly.entity.User;
import com.connectly.repository.*;
import com.connectly.security.JwtTokenProvider;
import com.connectly.service.ChatService;
import com.connectly.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GroupMessagingTests {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRequestRepository connectionRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatParticipantRepository participantRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageAttachmentRepository attachmentRepository;

    @Autowired
    private StarredMessageRepository starredMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ChatService chatService;

    private User userA; // Admin & Creator
    private User userB; // Member
    private User userC; // Member
    private User userOutsider; // Non-member
    private String tokenA;
    private String tokenB;
    private String tokenC;
    private String tokenOutsider;

    private GroupResponse groupResponse;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        starredMessageRepository.deleteAll();
        attachmentRepository.deleteAll();
        messageRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        participantRepository.deleteAll();
        chatRepository.deleteAll();
        connectionRepository.deleteAll();
        userRepository.deleteAll();

        // User A (Creator & Admin)
        userA = new User("Alice Walker", "alice@connectly.io", "alice_w", passwordEncoder.encode("Pass@1234"));
        userA = userRepository.save(userA);
        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail());

        // User B (Member)
        userB = new User("Bob Vance", "bob@connectly.io", "bob_v", passwordEncoder.encode("Pass@1234"));
        userB = userRepository.save(userB);
        tokenB = jwtTokenProvider.generateToken(userB.getId(), userB.getEmail());

        // User C (Member)
        userC = new User("Charlie Brown", "charlie@connectly.io", "charlie_b", passwordEncoder.encode("Pass@1234"));
        userC = userRepository.save(userC);
        tokenC = jwtTokenProvider.generateToken(userC.getId(), userC.getEmail());

        // User Outsider (Not in group)
        userOutsider = new User("Oscar Wilde", "oscar@connectly.io", "oscar_w", passwordEncoder.encode("Pass@1234"));
        userOutsider = userRepository.save(userOutsider);
        tokenOutsider = jwtTokenProvider.generateToken(userOutsider.getId(), userOutsider.getEmail());

        // Create Group with Alice as Admin and Bob + Charlie as members
        CreateGroupRequest groupReq = new CreateGroupRequest(
                "Engineering Team",
                "Frontend and Backend discussion group",
                Arrays.asList(userB.getId(), userC.getId())
        );
        groupResponse = groupService.createGroup(groupReq, userA);
    }

    private WebSocketStompClient createStompClient() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
        return stompClient;
    }

    private StompHeaders createConnectHeaders(String token) {
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    @Test
    @DisplayName("Create Group: Verify Creator is ADMIN, Members added, and Group details stored")
    void testCreateGroup_Success() throws Exception {
        CreateGroupRequest newGroupReq = new CreateGroupRequest(
                "Design Systems",
                "UI/UX Design discussions",
                Collections.singletonList(userB.getId())
        );

        mockMvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGroupReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Design Systems")))
                .andExpect(jsonPath("$.data.description", is("UI/UX Design discussions")))
                .andExpect(jsonPath("$.data.admin", is(true)))
                .andExpect(jsonPath("$.data.memberCount", is(2)))
                .andExpect(jsonPath("$.data.members[0].role", is("ADMIN")));
    }

    @Test
    @DisplayName("Group Messaging: Multiple users receive real-time message via STOMP WebSocket")
    void testGroupMessaging_MultipleUsers_RealTime() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";
        Long chatId = groupResponse.getChatId();

        // 1. User B connects and subscribes to group chat topic
        WebSocketStompClient clientB = createStompClient();
        CompletableFuture<StompSession> sessionBFuture = new CompletableFuture<>();
        clientB.connectAsync(wsUrl, new WebSocketHttpHeaders(), createConnectHeaders(tokenB), new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionBFuture.complete(session);
            }
        });
        StompSession sessionB = sessionBFuture.get(5, TimeUnit.SECONDS);

        CompletableFuture<WsEvent> msgFutureB = new CompletableFuture<>();
        sessionB.subscribe("/topic/chat." + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return WsEvent.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                msgFutureB.complete((WsEvent) payload);
            }
        });

        // 2. User C connects and subscribes to group chat topic
        WebSocketStompClient clientC = createStompClient();
        CompletableFuture<StompSession> sessionCFuture = new CompletableFuture<>();
        clientC.connectAsync(wsUrl, new WebSocketHttpHeaders(), createConnectHeaders(tokenC), new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionCFuture.complete(session);
            }
        });
        StompSession sessionC = sessionCFuture.get(5, TimeUnit.SECONDS);

        CompletableFuture<WsEvent> msgFutureC = new CompletableFuture<>();
        sessionC.subscribe("/topic/chat." + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return WsEvent.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                msgFutureC.complete((WsEvent) payload);
            }
        });

        // 3. User A connects and broadcasts message to /app/chat.sendMessage
        WebSocketStompClient clientA = createStompClient();
        CompletableFuture<StompSession> sessionAFuture = new CompletableFuture<>();
        clientA.connectAsync(wsUrl, new WebSocketHttpHeaders(), createConnectHeaders(tokenA), new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionAFuture.complete(session);
            }
        });
        StompSession sessionA = sessionAFuture.get(5, TimeUnit.SECONDS);

        WsChatMessagePayload payload = new WsChatMessagePayload();
        payload.setChatId(chatId);
        payload.setContent("Team meeting starts in 10 minutes!");

        sessionA.send("/app/chat.sendMessage", payload);

        // 4. Verify both User B and User C receive the real-time event
        WsEvent eventB = msgFutureB.get(5, TimeUnit.SECONDS);
        assertNotNull(eventB);
        assertEquals("NEW_MESSAGE", eventB.getType());

        WsEvent eventC = msgFutureC.get(5, TimeUnit.SECONDS);
        assertNotNull(eventC);
        assertEquals("NEW_MESSAGE", eventC.getType());

        // 5. Verify message is persisted in database
        List<Message> groupMessages = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatId);
        assertEquals(1, groupMessages.size());
        assertEquals("Team meeting starts in 10 minutes!", groupMessages.get(0).getContent());
        assertEquals(userA.getId(), groupMessages.get(0).getSender().getId());

        sessionA.disconnect();
        sessionB.disconnect();
        sessionC.disconnect();
    }

    @Test
    @DisplayName("Group Members: Admin can add new members; Non-Admin is REJECTED")
    void testAddMembers_AdminAndNonAdmin() throws Exception {
        User userD = new User("David Miller", "david@connectly.io", "david_m", passwordEncoder.encode("Pass@1234"));
        userD = userRepository.save(userD);

        AddGroupMembersRequest addReq = new AddGroupMembersRequest(Collections.singletonList(userD.getId()));

        // 1. Non-admin (User B) attempts to add member -> REJECTED
        mockMvc.perform(post("/api/v1/groups/" + groupResponse.getId() + "/members")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("administrators")));

        // 2. Admin (User A) adds member -> SUCCESS
        mockMvc.perform(post("/api/v1/groups/" + groupResponse.getId() + "/members")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4)));

        // Verify member count in database
        assertEquals(4, groupMemberRepository.findByGroupId(groupResponse.getId()).size());
    }

    @Test
    @DisplayName("Group Settings: Admin can update group info; Non-Admin is REJECTED")
    void testUpdateGroupInfo_AdminAndNonAdmin() throws Exception {
        UpdateGroupRequest updateReq = new UpdateGroupRequest(
                "Core Engineering Team",
                "Updated description for the team",
                "/api/v1/media/avatars/group.png"
        );

        // 1. Non-admin (User B) attempts update -> REJECTED
        mockMvc.perform(put("/api/v1/groups/" + groupResponse.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isUnauthorized());

        // 2. Admin (User A) updates -> SUCCESS
        mockMvc.perform(put("/api/v1/groups/" + groupResponse.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Core Engineering Team")))
                .andExpect(jsonPath("$.data.description", is("Updated description for the team")));

        Group updatedGroup = groupRepository.findById(groupResponse.getId()).orElseThrow();
        assertEquals("Core Engineering Team", updatedGroup.getName());
    }

    @Test
    @DisplayName("Group Members: Admin can remove members; Member can leave group")
    void testRemoveMemberAndLeaveGroup() throws Exception {
        // 1. Non-admin (User B) attempts to remove User C -> REJECTED
        mockMvc.perform(delete("/api/v1/groups/" + groupResponse.getId() + "/members/" + userC.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isUnauthorized());

        // 2. Admin (User A) removes User C -> SUCCESS
        mockMvc.perform(delete("/api/v1/groups/" + groupResponse.getId() + "/members/" + userC.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        assertFalse(groupMemberRepository.existsByGroupIdAndUserId(groupResponse.getId(), userC.getId()));

        // 3. User B leaves group -> SUCCESS
        mockMvc.perform(post("/api/v1/groups/" + groupResponse.getId() + "/leave")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        assertFalse(groupMemberRepository.existsByGroupIdAndUserId(groupResponse.getId(), userB.getId()));
    }

    @Test
    @DisplayName("Group Security: Outsider cannot view or send messages in group")
    void testOutsiderCannotAccessGroup() throws Exception {
        Long chatId = groupResponse.getChatId();

        // 1. Outsider attempts to read messages -> REJECTED
        mockMvc.perform(get("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + tokenOutsider))
                .andExpect(status().isUnauthorized());

        // 2. Outsider attempts to send message -> REJECTED
        SendMessageRequest sendReq = new SendMessageRequest("Sneaking in!");
        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + tokenOutsider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isUnauthorized());
    }
}
