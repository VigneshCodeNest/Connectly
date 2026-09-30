package com.connectly;

import com.connectly.dto.request.CreateDirectChatRequest;
import com.connectly.dto.request.RegisterRequest;
import com.connectly.dto.request.SendConnectionRequest;
import com.connectly.dto.request.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ChatControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String userAToken;
    private Long userAId;

    private String userBToken;
    private Long userBId;

    private String userCToken;
    private Long userCId;

    @BeforeEach
    void setUp() throws Exception {
        // Register User A (Arun)
        RegisterRequest uA = new RegisterRequest("Arun Kumar", "arun", "arun@example.com", "Password@123");
        MvcResult rA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uA)))
                .andExpect(status().isCreated()).andReturn();
        userAToken = objectMapper.readTree(rA.getResponse().getContentAsString()).get("data").get("token").asText();
        userAId = objectMapper.readTree(rA.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        // Register User B (Priya)
        RegisterRequest uB = new RegisterRequest("Priya", "priya", "priya@example.com", "Password@123");
        MvcResult rB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uB)))
                .andExpect(status().isCreated()).andReturn();
        userBToken = objectMapper.readTree(rB.getResponse().getContentAsString()).get("data").get("token").asText();
        userBId = objectMapper.readTree(rB.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        // Register User C (Stranger / Not Connected)
        RegisterRequest uC = new RegisterRequest("Alex", "alex", "alex@example.com", "Password@123");
        MvcResult rC = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uC)))
                .andExpect(status().isCreated()).andReturn();
        userCToken = objectMapper.readTree(rC.getResponse().getContentAsString()).get("data").get("token").asText();
        userCId = objectMapper.readTree(rC.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
    }

    @Test
    void testCreateDirectChatBetweenConnectedUsersSuccess() throws Exception {
        // 1. Establish Connection between User A and User B
        MvcResult connRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(connRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        // 2. Open Direct Chat
        mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userBId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("DIRECT"))
                .andExpect(jsonPath("$.data.name").value("Priya"))
                .andExpect(jsonPath("$.data.otherUser.id").value(userBId));
    }

    @Test
    void testCreateDirectChatBetweenNonConnectedUsersFails() throws Exception {
        // User A and User C are not connected -> must be rejected by backend
        mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userCId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("because you are not connected")));
    }

    @Test
    void testCreateDirectChatSelfFails() throws Exception {
        mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userAId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You cannot start a private chat with yourself"));
    }

    @Test
    void testGetUserChatsListAndDetails() throws Exception {
        // Connect User A and User B
        MvcResult connRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(connRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        // Create direct chat
        MvcResult chatRes = mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userBId))))
                .andExpect(status().isOk()).andReturn();
        Long chatId = objectMapper.readTree(chatRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Verify User A sees chat in list
        mockMvc.perform(get("/api/v1/chats")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(chatId));

        // Get chat details
        mockMvc.perform(get("/api/v1/chats/" + chatId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(chatId));

        // Get participants
        mockMvc.perform(get("/api/v1/chats/" + chatId + "/participants")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetChatDetailsUnauthorizedForNonParticipant() throws Exception {
        // Connect User A and User B and create chat
        MvcResult connRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(connRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        MvcResult chatRes = mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userBId))))
                .andExpect(status().isOk()).andReturn();
        Long chatId = objectMapper.readTree(chatRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // User C (unrelated third party) tries to view User A & B's chat -> 401 Unauthorized
        mockMvc.perform(get("/api/v1/chats/" + chatId)
                        .header("Authorization", "Bearer " + userCToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not a participant in this conversation"));
    }

    @Test
    void testSendMessageAndRetrieveHistory() throws Exception {
        // Connect User A and User B
        MvcResult connRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(connRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        MvcResult chatRes = mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userBId))))
                .andExpect(status().isOk()).andReturn();
        Long chatId = objectMapper.readTree(chatRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // User A sends message
        SendMessageRequest msgReq = new SendMessageRequest("Hey Priya, how are you?");
        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Hey Priya, how are you?"))
                .andExpect(jsonPath("$.data.sender.id").value(userAId));

        // User B retrieves messages
        mockMvc.perform(get("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].content").value("Hey Priya, how are you?"));
    }

    @Test
    void testSendMessageUnauthorizedForNonParticipant() throws Exception {
        // Connect User A and User B
        MvcResult connRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(connRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        MvcResult chatRes = mockMvc.perform(post("/api/v1/chats/direct")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDirectChatRequest(userBId))))
                .andExpect(status().isOk()).andReturn();
        Long chatId = objectMapper.readTree(chatRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // User C tries to send message into User A and B's chat
        mockMvc.perform(post("/api/v1/chats/" + chatId + "/messages")
                        .header("Authorization", "Bearer " + userCToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendMessageRequest("Intruder message"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
