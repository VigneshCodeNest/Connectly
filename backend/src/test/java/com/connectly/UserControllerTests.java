package com.connectly;

import com.connectly.dto.request.*;
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
public class UserControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String user1Token;
    private String user2Token;
    private Long user2Id;

    @BeforeEach
    void setUp() throws Exception {
        // Register User 1
        RegisterRequest u1 = new RegisterRequest("User One", "user1", "user1@example.com", "Password@123");
        MvcResult r1 = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u1)))
                .andExpect(status().isCreated())
                .andReturn();
        user1Token = objectMapper.readTree(r1.getResponse().getContentAsString()).get("data").get("token").asText();

        // Register User 2
        RegisterRequest u2 = new RegisterRequest("User Two", "user2", "user2@example.com", "Password@123");
        MvcResult r2 = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u2)))
                .andExpect(status().isCreated())
                .andReturn();
        user2Token = objectMapper.readTree(r2.getResponse().getContentAsString()).get("data").get("token").asText();
        user2Id = objectMapper.readTree(r2.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
    }

    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user1@example.com"))
                .andExpect(jsonPath("$.data.username").value("@user1"));
    }

    @Test
    void testUpdateProfile() throws Exception {
        UpdateProfileRequest update = new UpdateProfileRequest("User One Updated", "Updated bio text", "https://img.com/avatar.jpg");

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("User One Updated"))
                .andExpect(jsonPath("$.data.bio").value("Updated bio text"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://img.com/avatar.jpg"));
    }

    @Test
    void testUpdateUsernameSuccess() throws Exception {
        UpdateUsernameRequest update = new UpdateUsernameRequest("user1_new");

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("@user1_new"));
    }

    @Test
    void testUpdateUsernameDuplicateConflict() throws Exception {
        UpdateUsernameRequest update = new UpdateUsernameRequest("user2"); // already taken by User 2

        mockMvc.perform(put("/api/v1/users/me/username")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username @user2 is already taken"));
    }

    @Test
    void testUpdateAvatar() throws Exception {
        UpdateAvatarRequest update = new UpdateAvatarRequest("https://cdn.example.com/new-pic.png");

        mockMvc.perform(put("/api/v1/users/me/avatar")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.example.com/new-pic.png"));
    }

    @Test
    void testUpdateBio() throws Exception {
        UpdateBioRequest update = new UpdateBioRequest("Focused on building high quality software");

        mockMvc.perform(put("/api/v1/users/me/bio")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bio").value("Focused on building high quality software"));
    }

    @Test
    void testSearchUserByEmailFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("email", "user2@example.com")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user2@example.com"))
                .andExpect(jsonPath("$.data.username").value("@user2"))
                .andExpect(jsonPath("$.data.self").value(false));
    }

    @Test
    void testSearchUserByEmailSelfCheck() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("email", "user1@example.com")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user1@example.com"))
                .andExpect(jsonPath("$.data.self").value(true));
    }

    @Test
    void testSearchUserByEmailNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/search")
                        .param("email", "nonexistent@unknown.com")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No user found with email: nonexistent@unknown.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + user2Id)
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(user2Id))
                .andExpect(jsonPath("$.data.email").value("user2@example.com"));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/99999")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
