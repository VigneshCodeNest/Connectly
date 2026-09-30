package com.connectly;

import com.connectly.constant.ConnectionStatus;
import com.connectly.constant.StatusType;
import com.connectly.dto.request.CreateStatusRequest;
import com.connectly.dto.response.StatusResponse;
import com.connectly.entity.ConnectionRequest;
import com.connectly.entity.Status;
import com.connectly.entity.User;
import com.connectly.repository.*;
import com.connectly.security.JwtTokenProvider;
import com.connectly.service.StatusService;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StatusControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRequestRepository connectionRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private StatusViewRepository statusViewRepository;

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
    private StatusService statusService;

    private User userA;
    private User userB;
    private User userC;
    private String tokenA;
    private String tokenB;
    private String tokenC;

    @BeforeEach
    void setUp() {
        statusViewRepository.deleteAll();
        statusRepository.deleteAll();
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

        // User C (Stranger / Not connected)
        userC = new User("Charlie Brown", "charlie@connectly.io", "charlie_b", passwordEncoder.encode("Pass@1234"));
        userC = userRepository.save(userC);
        tokenC = jwtTokenProvider.generateToken(userC.getId(), userC.getEmail());

        // Connect User A and User B
        ConnectionRequest connAB = new ConnectionRequest(userA, userB, ConnectionStatus.ACCEPTED);
        connectionRepository.save(connAB);
    }

    @Test
    @DisplayName("Create Status: Create TEXT status with 24-hour expiration")
    void testCreateTextStatus_Success() throws Exception {
        CreateStatusRequest req = new CreateStatusRequest(StatusType.TEXT, "Enjoying the sunny weekend!");
        req.setBackgroundColor("#4A0E4E");

        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.statusType", is("TEXT")))
                .andExpect(jsonPath("$.data.content", is("Enjoying the sunny weekend!")))
                .andExpect(jsonPath("$.data.backgroundColor", is("#4A0E4E")))
                .andExpect(jsonPath("$.data.expired", is(false)))
                .andExpect(jsonPath("$.data.expiresAt", notNullValue()));

        List<Status> statuses = statusRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(userA.getId(), LocalDateTime.now());
        assertEquals(1, statuses.size());
        assertEquals(StatusType.TEXT, statuses.get(0).getStatusType());
    }

    @Test
    @DisplayName("Create Status: Create IMAGE status with mediaUrl")
    void testCreateImageStatus_Success() throws Exception {
        CreateStatusRequest req = new CreateStatusRequest(
                StatusType.IMAGE,
                "Sunset view from the office",
                "/api/v1/media/images/sunset_hdr.jpg"
        );

        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.statusType", is("IMAGE")))
                .andExpect(jsonPath("$.data.mediaUrl", is("/api/v1/media/images/sunset_hdr.jpg")));
    }

    @Test
    @DisplayName("Status Validation: Reject empty text content or missing media URL")
    void testCreateStatus_InvalidPayload_Rejected() throws Exception {
        // Empty text status
        CreateStatusRequest emptyText = new CreateStatusRequest(StatusType.TEXT, "   ");
        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyText)))
                .andExpect(status().isBadRequest());

        // Empty image status
        CreateStatusRequest emptyImage = new CreateStatusRequest(StatusType.IMAGE, "Caption", null);
        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyImage)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Status Validation: Unsupported media types (VIDEO, AUDIO, MUSIC) must be REJECTED")
    void testCreateStatus_UnsupportedMediaType_Rejected() throws Exception {
        // VIDEO status type attempt
        String videoPayload = """
                {
                    "statusType": "VIDEO",
                    "content": "Check out this clip",
                    "mediaUrl": "/api/v1/media/videos/clip.mp4"
                }
                """;
        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(videoPayload))
                .andExpect(status().isBadRequest());

        // AUDIO status type attempt
        String audioPayload = """
                {
                    "statusType": "AUDIO",
                    "content": "Listen to this audio note",
                    "mediaUrl": "/api/v1/media/audio/voice.mp3"
                }
                """;
        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(audioPayload))
                .andExpect(status().isBadRequest());

        // MUSIC status type attempt
        String musicPayload = """
                {
                    "statusType": "MUSIC",
                    "content": "My favorite track",
                    "mediaUrl": "/api/v1/media/audio/song.mp3"
                }
                """;
        mockMvc.perform(post("/api/v1/statuses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(musicPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Status View Tracking: User B views User A's status, view recorded and counted")
    void testViewStatus_AndTrackViews() throws Exception {
        CreateStatusRequest req = new CreateStatusRequest(StatusType.TEXT, "Coffee break!");
        StatusResponse createdStatus = statusService.createStatus(req, userA);

        // 1. User B views status
        mockMvc.perform(post("/api/v1/statuses/" + createdStatus.getId() + "/view")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount", is(1)))
                .andExpect(jsonPath("$.data.viewedByCurrentUser", is(true)));

        // 2. User A (Owner) inspects who viewed their status
        mockMvc.perform(get("/api/v1/statuses/" + createdStatus.getId() + "/viewers")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].viewer.username", is("bob_v")))
                .andExpect(jsonPath("$.data[0].viewedAt", notNullValue()));
    }

    @Test
    @DisplayName("Status Security: Non-owner cannot view viewer list (Unauthorized)")
    void testGetStatusViewers_NonOwner_Forbidden() throws Exception {
        CreateStatusRequest req = new CreateStatusRequest(StatusType.TEXT, "Private status");
        StatusResponse createdStatus = statusService.createStatus(req, userA);

        // User B attempts to access viewers list of User A's status -> REJECTED
        mockMvc.perform(get("/api/v1/statuses/" + createdStatus.getId() + "/viewers")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("owner")));
    }

    @Test
    @DisplayName("Delete Status: Owner can delete status; Non-owner is REJECTED")
    void testDeleteStatus_OwnerAndNonOwner() throws Exception {
        CreateStatusRequest req = new CreateStatusRequest(StatusType.TEXT, "Status to delete");
        StatusResponse createdStatus = statusService.createStatus(req, userA);

        // 1. User B attempts to delete User A's status -> REJECTED
        mockMvc.perform(delete("/api/v1/statuses/" + createdStatus.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isUnauthorized());

        assertTrue(statusRepository.existsById(createdStatus.getId()));

        // 2. User A deletes own status -> SUCCESS
        mockMvc.perform(delete("/api/v1/statuses/" + createdStatus.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        assertFalse(statusRepository.existsById(createdStatus.getId()));
    }

    @Test
    @DisplayName("Status Expiration: Status becomes unavailable after 24 hours and is purged")
    void testStatusExpiration_24Hours() throws Exception {
        // Create an already expired status (expiresAt 10 minutes in the past)
        Status expiredStatus = new Status();
        expiredStatus.setUser(userA);
        expiredStatus.setStatusType(StatusType.TEXT);
        expiredStatus.setContent("Expired announcement");
        expiredStatus.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        expiredStatus = statusRepository.save(expiredStatus);

        // Attempting to fetch or view expired status returns 404 Not Found
        mockMvc.perform(get("/api/v1/statuses/" + expiredStatus.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Purge expired statuses
        int purged = statusService.purgeExpiredStatuses();
        assertEquals(1, purged);
        assertFalse(statusRepository.existsById(expiredStatus.getId()));
    }

    @Test
    @DisplayName("Status Feed: Retrieve active statuses from connected contacts")
    void testStatusFeed_ConnectedContacts() throws Exception {
        // User B posts a status
        CreateStatusRequest reqB = new CreateStatusRequest(StatusType.TEXT, "Bob's active status");
        statusService.createStatus(reqB, userB);

        // User A fetches status feed
        mockMvc.perform(get("/api/v1/statuses/feed")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].user.username", is("bob_v")))
                .andExpect(jsonPath("$.data[0].statuses", hasSize(1)))
                .andExpect(jsonPath("$.data[0].statuses[0].content", is("Bob's active status")));
    }
}
