package com.connectly;

import com.connectly.dto.request.RegisterRequest;
import com.connectly.dto.request.SendConnectionRequest;
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
public class ConnectionControllerTests {

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

        // Register User C (Alex)
        RegisterRequest uC = new RegisterRequest("Alex Morgan", "alex", "alex@example.com", "Password@123");
        MvcResult rC = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uC)))
                .andExpect(status().isCreated()).andReturn();
        userCToken = objectMapper.readTree(rC.getResponse().getContentAsString()).get("data").get("token").asText();
        userCId = objectMapper.readTree(rC.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
    }

    @Test
    void testSendConnectionRequestSuccess() throws Exception {
        SendConnectionRequest request = new SendConnectionRequest(userBId);

        mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.sender.id").value(userAId))
                .andExpect(jsonPath("$.data.receiver.id").value(userBId));
    }

    @Test
    void testSendConnectionRequestByEmailSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/connections/request/email")
                        .param("email", "priya@example.com")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void testSelfConnectionRequestError() throws Exception {
        SendConnectionRequest request = new SendConnectionRequest(userAId);

        mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You cannot send a connection request to yourself"));
    }

    @Test
    void testDuplicatePendingConnectionRequest() throws Exception {
        SendConnectionRequest request = new SendConnectionRequest(userBId);

        // 1st request
        mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 2nd duplicate request
        mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You have already sent a connection request to Priya"));
    }

    @Test
    void testAcceptConnectionRequestSuccess() throws Exception {
        // User A sends request to User B
        MvcResult sendResult = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated())
                .andReturn();

        Long requestId = objectMapper.readTree(sendResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // User B accepts request
        mockMvc.perform(put("/api/v1/connections/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // Verify User A & User B both see each other in connected friends
        mockMvc.perform(get("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].connectedUser.id").value(userBId));

        mockMvc.perform(get("/api/v1/connections")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].connectedUser.id").value(userAId));
    }

    @Test
    void testAcceptConnectionUnauthorizedByThirdParty() throws Exception {
        // User A sends request to User B
        MvcResult sendResult = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated())
                .andReturn();

        Long requestId = objectMapper.readTree(sendResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // User C (unrelated third party) tries to accept User B's request -> 401 Unauthorized
        mockMvc.perform(put("/api/v1/connections/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + userCToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to accept this connection request"));
    }

    @Test
    void testRejectConnectionRequestSuccess() throws Exception {
        // User A sends request to User B
        MvcResult sendResult = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated())
                .andReturn();

        Long requestId = objectMapper.readTree(sendResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // User B rejects request
        mockMvc.perform(put("/api/v1/connections/" + requestId + "/reject")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        // Verify connected friends is empty
        mockMvc.perform(get("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testAlreadyConnectedUserDuplicateError() throws Exception {
        // User A connects with User B
        MvcResult sendResult = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long requestId = objectMapper.readTree(sendResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        // Attempting to send request again when already connected
        mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are already connected with Priya"));
    }

    @Test
    void testGetConnectionStatusFlow() throws Exception {
        // Status before request -> NONE
        mockMvc.perform(get("/api/v1/connections/status/" + userBId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("NONE"));

        // User A sends request
        MvcResult sendRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(sendRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // From User A perspective -> REQUEST_SENT
        mockMvc.perform(get("/api/v1/connections/status/" + userBId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REQUEST_SENT"));

        // From User B perspective -> REQUEST_RECEIVED
        mockMvc.perform(get("/api/v1/connections/status/" + userAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REQUEST_RECEIVED"));

        // User B accepts
        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        // Both now see CONNECTED
        mockMvc.perform(get("/api/v1/connections/status/" + userBId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONNECTED"));
    }

    @Test
    void testRemoveConnectionSuccess() throws Exception {
        // Connect User A and User B
        MvcResult sendRes = mockMvc.perform(post("/api/v1/connections/request")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SendConnectionRequest(userBId))))
                .andExpect(status().isCreated()).andReturn();
        Long reqId = objectMapper.readTree(sendRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(put("/api/v1/connections/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk());

        // Remove connection
        mockMvc.perform(delete("/api/v1/connections/" + reqId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify connected friends list is now empty
        mockMvc.perform(get("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testUnauthorizedAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/connections"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
