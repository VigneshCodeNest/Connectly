package com.connectly;

import com.connectly.constant.ConnectionStatus;
import com.connectly.constant.MessageStatus;
import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ChatResponse;
import com.connectly.dto.response.MessageResponse;
import com.connectly.dto.websocket.WsChatMessagePayload;
import com.connectly.dto.websocket.WsEvent;
import com.connectly.dto.websocket.WsStatusUpdatePayload;
import com.connectly.entity.ConnectionRequest;
import com.connectly.entity.Message;
import com.connectly.entity.User;
import com.connectly.repository.*;
import com.connectly.security.JwtTokenProvider;
import com.connectly.service.ChatService;
import com.connectly.service.PresenceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketChatTests {

    @LocalServerPort
    private int port;

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
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private StarredMessageRepository starredMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ChatService chatService;

    @Autowired
    private PresenceService presenceService;

    private User userA;
    private User userB;
    private User userC;
    private String tokenA;
    private String tokenB;
    private String tokenC;
    private ChatResponse directChat;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        starredMessageRepository.deleteAll();
        messageRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        participantRepository.deleteAll();
        chatRepository.deleteAll();
        connectionRepository.deleteAll();
        userRepository.deleteAll();

        // Create User A
        userA = new User("Alice Walker", "alice@connectly.io", "alice_w", passwordEncoder.encode("Pass@1234"));
        userA = userRepository.save(userA);
        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail());

        // Create User B
        userB = new User("Bob Vance", "bob@connectly.io", "bob_v", passwordEncoder.encode("Pass@1234"));
        userB = userRepository.save(userB);
        tokenB = jwtTokenProvider.generateToken(userB.getId(), userB.getEmail());

        // Create User C (Unauthorized outsider)
        userC = new User("Charlie Brown", "charlie@connectly.io", "charlie_b", passwordEncoder.encode("Pass@1234"));
        userC = userRepository.save(userC);
        tokenC = jwtTokenProvider.generateToken(userC.getId(), userC.getEmail());

        // Create Accepted Connection between User A and User B
        ConnectionRequest connection = new ConnectionRequest(userA, userB, ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);

        // Create Direct Chat between User A and User B
        CreateDirectChatRequest directReq = new CreateDirectChatRequest();
        directReq.setTargetUserId(userB.getId());
        directChat = chatService.getOrCreateDirectChat(directReq, userA);
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
    @DisplayName("STOMP WebSocket: Authenticated connection succeeds for User A and User B")
    void testWebSocketConnection_Success() throws Exception {
        WebSocketStompClient clientA = createStompClient();
        String wsUrl = "ws://localhost:" + port + "/ws";

        StompHeaders headersA = createConnectHeaders(tokenA);
        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();

        clientA.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersA, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFuture.complete(session);
            }
        });

        StompSession sessionA = sessionFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(sessionA);
        assertTrue(sessionA.isConnected());
        sessionA.disconnect();
    }

    @Test
    @DisplayName("STOMP WebSocket: User A → User B Real-Time Message Exchange & MySQL Persistence")
    void testRealTimeMessageExchange_UserA_to_UserB() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";
        Long chatId = directChat.getId();

        // 1. User B connects and subscribes to /topic/chat.{chatId}
        WebSocketStompClient clientB = createStompClient();
        StompHeaders headersB = createConnectHeaders(tokenB);
        CompletableFuture<StompSession> sessionBFuture = new CompletableFuture<>();

        clientB.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersB, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionBFuture.complete(session);
            }
        });
        StompSession sessionB = sessionBFuture.get(5, TimeUnit.SECONDS);

        CompletableFuture<WsEvent> receivedMessageFuture = new CompletableFuture<>();
        sessionB.subscribe("/topic/chat." + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WsEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessageFuture.complete((WsEvent) payload);
            }
        });

        // 2. User A connects to WebSocket and sends message to /app/chat.sendMessage
        WebSocketStompClient clientA = createStompClient();
        StompHeaders headersA = createConnectHeaders(tokenA);
        CompletableFuture<StompSession> sessionAFuture = new CompletableFuture<>();

        clientA.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersA, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionAFuture.complete(session);
            }
        });
        StompSession sessionA = sessionAFuture.get(5, TimeUnit.SECONDS);

        WsChatMessagePayload msgPayload = new WsChatMessagePayload();
        msgPayload.setChatId(chatId);
        msgPayload.setContent("Hello Bob, this is Alice via WebSocket!");

        sessionA.send("/app/chat.sendMessage", msgPayload);

        // 3. User B receives the message in real time
        WsEvent event = receivedMessageFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(event);
        assertEquals("NEW_MESSAGE", event.getType());

        // 4. Verify message is persisted in MySQL/H2
        List<Message> persistedMessages = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatId);
        assertEquals(1, persistedMessages.size());
        Message saved = persistedMessages.get(0);
        assertEquals("Hello Bob, this is Alice via WebSocket!", saved.getContent());
        assertEquals(userA.getId(), saved.getSender().getId());
        assertEquals(MessageStatus.SENT, saved.getStatus());

        sessionA.disconnect();
        sessionB.disconnect();
    }

    @Test
    @DisplayName("STOMP WebSocket: User B → User A Real-Time Reply & MySQL Persistence")
    void testRealTimeMessageExchange_UserB_to_UserA() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";
        Long chatId = directChat.getId();

        // 1. User A connects and subscribes to /topic/chat.{chatId}
        WebSocketStompClient clientA = createStompClient();
        StompHeaders headersA = createConnectHeaders(tokenA);
        CompletableFuture<StompSession> sessionAFuture = new CompletableFuture<>();

        clientA.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersA, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionAFuture.complete(session);
            }
        });
        StompSession sessionA = sessionAFuture.get(5, TimeUnit.SECONDS);

        CompletableFuture<WsEvent> receivedMessageFuture = new CompletableFuture<>();
        sessionA.subscribe("/topic/chat." + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WsEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessageFuture.complete((WsEvent) payload);
            }
        });

        // 2. User B connects and sends message to /app/chat.sendMessage
        WebSocketStompClient clientB = createStompClient();
        StompHeaders headersB = createConnectHeaders(tokenB);
        CompletableFuture<StompSession> sessionBFuture = new CompletableFuture<>();

        clientB.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersB, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionBFuture.complete(session);
            }
        });
        StompSession sessionB = sessionBFuture.get(5, TimeUnit.SECONDS);

        WsChatMessagePayload msgPayload = new WsChatMessagePayload();
        msgPayload.setChatId(chatId);
        msgPayload.setContent("Hey Alice! Loud and clear!");

        sessionB.send("/app/chat.sendMessage", msgPayload);

        // 3. User A receives the reply in real time
        WsEvent event = receivedMessageFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(event);
        assertEquals("NEW_MESSAGE", event.getType());

        // 4. Verify message is persisted in MySQL
        List<Message> persistedMessages = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatId);
        assertEquals(1, persistedMessages.size());
        Message saved = persistedMessages.get(0);
        assertEquals("Hey Alice! Loud and clear!", saved.getContent());
        assertEquals(userB.getId(), saved.getSender().getId());
        assertEquals(MessageStatus.SENT, saved.getStatus());

        sessionA.disconnect();
        sessionB.disconnect();
    }

    @Test
    @DisplayName("Message State Progression: SENT → DELIVERED → READ real-time delivery and DB persistence")
    void testMessageStateProgression_Sent_Delivered_Read() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";
        Long chatId = directChat.getId();

        // Pre-create a message from User A to User B
        SendMessageRequest req = new SendMessageRequest();
        req.setContent("Test receipt progression");
        MessageResponse sentMsg = chatService.sendMessage(chatId, req, userA);
        assertEquals(MessageStatus.SENT, sentMsg.getStatus());

        // 1. User A connects to listen for status updates (delivered & read)
        WebSocketStompClient clientA = createStompClient();
        StompHeaders headersA = createConnectHeaders(tokenA);
        CompletableFuture<StompSession> sessionAFuture = new CompletableFuture<>();

        clientA.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersA, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionAFuture.complete(session);
            }
        });
        StompSession sessionA = sessionAFuture.get(5, TimeUnit.SECONDS);

        BlockingQueue<WsEvent> eventQueue = new LinkedBlockingQueue<>();
        sessionA.subscribe("/topic/chat." + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return WsEvent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                eventQueue.add((WsEvent) payload);
            }
        });

        // 2. User B connects and sends DELIVERED receipt via /app/chat.markDelivered
        WebSocketStompClient clientB = createStompClient();
        StompHeaders headersB = createConnectHeaders(tokenB);
        CompletableFuture<StompSession> sessionBFuture = new CompletableFuture<>();

        clientB.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersB, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionBFuture.complete(session);
            }
        });
        StompSession sessionB = sessionBFuture.get(5, TimeUnit.SECONDS);

        WsStatusUpdatePayload deliveredPayload = new WsStatusUpdatePayload();
        deliveredPayload.setChatId(chatId);
        deliveredPayload.setMessageId(sentMsg.getId());

        sessionB.send("/app/chat.markDelivered", deliveredPayload);

        // Verify User A receives MESSAGE_DELIVERED event
        WsEvent deliveredEvent = eventQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(deliveredEvent);
        assertEquals("MESSAGE_DELIVERED", deliveredEvent.getType());

        // Verify status in Database is DELIVERED
        Message dbMsgAfterDelivered = messageRepository.findById(sentMsg.getId()).orElseThrow();
        assertEquals(MessageStatus.DELIVERED, dbMsgAfterDelivered.getStatus());

        // 3. User B sends READ receipt via /app/chat.markRead
        WsStatusUpdatePayload readPayload = new WsStatusUpdatePayload();
        readPayload.setChatId(chatId);

        sessionB.send("/app/chat.markRead", readPayload);

        // Verify User A receives MESSAGE_READ event
        WsEvent readEvent = eventQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(readEvent);
        assertEquals("MESSAGE_READ", readEvent.getType());

        // Verify status in Database is READ
        Message dbMsgAfterRead = messageRepository.findById(sentMsg.getId()).orElseThrow();
        assertEquals(MessageStatus.READ, dbMsgAfterRead.getStatus());

        sessionA.disconnect();
        sessionB.disconnect();
    }

    @Test
    @DisplayName("Destination Authorization: Unauthorized User C is REJECTED from subscribing to chat topic")
    void testDestinationAuthorization_UnauthorizedUserSubscription_Rejected() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws";
        Long chatId = directChat.getId();

        // User C is NOT a participant in directChat between Alice and Bob
        WebSocketStompClient clientC = createStompClient();
        StompHeaders headersC = createConnectHeaders(tokenC);

        CompletableFuture<StompSession> sessionCFuture = new CompletableFuture<>();
        CompletableFuture<Throwable> errorFuture = new CompletableFuture<>();

        clientC.connectAsync(wsUrl, new WebSocketHttpHeaders(), headersC, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionCFuture.complete(session);
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (headers.get("message") != null) {
                    errorFuture.complete(new RuntimeException(headers.getFirst("message")));
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                errorFuture.complete(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                errorFuture.complete(exception);
            }
        });

        StompSession sessionC = sessionCFuture.get(5, TimeUnit.SECONDS);
        assertTrue(sessionC.isConnected());

        // Attempt unauthorized subscription to User A and B's chat
        sessionC.subscribe("/topic/chat." + chatId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {}
        });

        // The broker should send an ERROR frame or terminate session due to AccessDeniedException
        Throwable error = errorFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(error);
        sessionC.disconnect();
    }

    @Test
    @DisplayName("Presence Service: Online/Offline state tracking and broadcasting")
    void testPresenceTracking() {
        assertFalse(presenceService.isUserOnline(userA.getId()));

        presenceService.handleUserConnected(userA.getId(), "sess-1");
        assertTrue(presenceService.isUserOnline(userA.getId()));

        // Second session for same user (e.g. mobile + desktop)
        presenceService.handleUserConnected(userA.getId(), "sess-2");
        assertTrue(presenceService.isUserOnline(userA.getId()));

        // Disconnect one session -> still online
        presenceService.handleUserDisconnected(userA.getId(), "sess-1");
        assertTrue(presenceService.isUserOnline(userA.getId()));

        // Disconnect last session -> offline
        presenceService.handleUserDisconnected(userA.getId(), "sess-2");
        assertFalse(presenceService.isUserOnline(userA.getId()));
    }
}
