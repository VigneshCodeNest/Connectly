package com.connectly;

import com.connectly.constant.ConnectionStatus;
import com.connectly.constant.MessageType;
import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.connectly.dto.response.ChatResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MediaControllerTests {

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

    private User userA;
    private User userB;
    private String tokenA;
    private ChatResponse directChat;

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

        // Create User A
        userA = new User("Alice Walker", "alice@connectly.io", "alice_w", passwordEncoder.encode("Pass@1234"));
        userA = userRepository.save(userA);
        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail());

        // Create User B
        userB = new User("Bob Vance", "bob@connectly.io", "bob_v", passwordEncoder.encode("Pass@1234"));
        userB = userRepository.save(userB);

        // Connect User A and User B
        ConnectionRequest connection = new ConnectionRequest(userA, userB, ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);

        CreateDirectChatRequest directReq = new CreateDirectChatRequest();
        directReq.setTargetUserId(userB.getId());
        directChat = chatService.getOrCreateDirectChat(directReq, userA);
    }

    @Test
    @DisplayName("Media Upload: Successful Image Upload and Metadata Generation")
    void testUploadImage_Success() throws Exception {
        byte[] imageBytes = new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46 };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vacation_photo.jpg",
                "image/jpeg",
                imageBytes
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("type", "image")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fileUrl", startsWith("/api/v1/media/images/")))
                .andExpect(jsonPath("$.data.fileName", is("vacation_photo.jpg")))
                .andExpect(jsonPath("$.data.mediaType", is("IMAGE")))
                .andExpect(jsonPath("$.data.category", is("images")))
                .andExpect(jsonPath("$.data.fileSize", is(imageBytes.length)));
    }

    @Test
    @DisplayName("Media Upload: Successful Video Upload and Retrieval")
    void testUploadAndRetrieveVideo_Success() throws Exception {
        byte[] videoBytes = "sample video binary content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo_clip.mp4",
                "video/mp4",
                videoBytes
        );

        String uploadResponse = mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("type", "video")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.mediaType", is("VIDEO")))
                .andExpect(jsonPath("$.data.category", is("videos")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String fileUrl = objectMapper.readTree(uploadResponse).path("data").path("fileUrl").asText();
        assertNotNull(fileUrl);

        // Retrieve the stored media
        mockMvc.perform(get(fileUrl))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("video/mp4")))
                .andExpect(content().bytes(videoBytes));
    }

    @Test
    @DisplayName("Media Upload: Successful Sticker Upload")
    void testUploadSticker_Success() throws Exception {
        byte[] stickerBytes = "sticker webp content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cute_cat.webp",
                "image/webp",
                stickerBytes
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("type", "sticker")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.mediaType", is("STICKER")))
                .andExpect(jsonPath("$.data.category", is("stickers")));
    }

    @Test
    @DisplayName("Media Security: Reject Executable Files (.exe, .sh, .php)")
    void testRejectExecutableFiles() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "MZ executable content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(exeFile)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("forbidden")));

        MockMultipartFile scriptFile = new MockMultipartFile(
                "file",
                "payload.sh",
                "text/x-shellscript",
                "#!/bin/bash echo hacked".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(scriptFile)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Chat Messaging: Create Image, Video, and Sticker Messages in Direct Chat")
    void testCreateMediaMessagesInChat() throws Exception {
        Long chatId = directChat.getId();

        // 1. Send Image Message
        SendMessageRequest imageReq = new SendMessageRequest();
        imageReq.setContent("Check out this sunset!");
        imageReq.setMessageType(MessageType.IMAGE);
        imageReq.setFileUrl("/api/v1/media/images/sunset.jpg");
        imageReq.setFileName("sunset.jpg");
        imageReq.setFileType("image/jpeg");
        imageReq.setFileSize(204800L);

        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(imageReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageType", is("IMAGE")))
                .andExpect(jsonPath("$.data.attachment.fileUrl", is("/api/v1/media/images/sunset.jpg")))
                .andExpect(jsonPath("$.data.attachment.fileName", is("sunset.jpg")));

        // 2. Send Video Message
        SendMessageRequest videoReq = new SendMessageRequest();
        videoReq.setContent("Recorded tutorial clip");
        videoReq.setMessageType(MessageType.VIDEO);
        videoReq.setFileUrl("/api/v1/media/videos/tutorial.mp4");
        videoReq.setFileName("tutorial.mp4");
        videoReq.setFileType("video/mp4");
        videoReq.setFileSize(5242880L);

        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(videoReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageType", is("VIDEO")))
                .andExpect(jsonPath("$.data.attachment.fileUrl", is("/api/v1/media/videos/tutorial.mp4")));

        // 3. Send Sticker Message (no text caption)
        SendMessageRequest stickerReq = new SendMessageRequest();
        stickerReq.setMessageType(MessageType.STICKER);
        stickerReq.setFileUrl("/api/v1/stickers/assets/heart_eyes.png");
        stickerReq.setFileName("heart_eyes.png");
        stickerReq.setFileType("image/png");

        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stickerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageType", is("STICKER")))
                .andExpect(jsonPath("$.data.attachment.fileUrl", is("/api/v1/stickers/assets/heart_eyes.png")));

        // Verify all 3 media messages are saved in database
        List<Message> messages = messageRepository.findByChatIdAndIsDeletedFalseOrderByCreatedAtAsc(chatId);
        assertEquals(3, messages.size());
        assertEquals(MessageType.IMAGE, messages.get(0).getMessageType());
        assertEquals(MessageType.VIDEO, messages.get(1).getMessageType());
        assertEquals(MessageType.STICKER, messages.get(2).getMessageType());
    }

    @Test
    @DisplayName("Stickers Catalog: Retrieve Available Sticker Packs")
    void testGetStickersCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/stickers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[0].id", is("pack-reactions")))
                .andExpect(jsonPath("$.data[0].stickers", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("Media Security: Unauthenticated Upload Request Rejected")
    void testUnauthenticatedUpload_Rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "some content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/media/upload").file(file))
                .andExpect(status().isUnauthorized());
    }
}
