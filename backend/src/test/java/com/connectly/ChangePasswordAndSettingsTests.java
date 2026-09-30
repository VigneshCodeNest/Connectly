package com.connectly;

import com.connectly.dto.request.ChangePasswordRequest;
import com.connectly.dto.request.LoginRequest;
import com.connectly.dto.request.UpdatePrivacySettingsRequest;
import com.connectly.dto.request.UpdateSettingsRequest;
import com.connectly.entity.User;
import com.connectly.repository.UserRepository;
import com.connectly.security.JwtTokenProvider;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChangePasswordAndSettingsTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User userA;
    private String tokenA;
    private final String originalPassword = "Pass@1234";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userA = new User("Alice Walker", "alice@connectly.io", "@alice_w", passwordEncoder.encode(originalPassword));
        userA.setColorAccent("#6366f1");
        userA = userRepository.save(userA);

        tokenA = jwtTokenProvider.generateToken(userA.getId(), userA.getEmail());
    }

    @Test
    @DisplayName("Change Password: Correct current password, matching confirmation -> Success")
    void testChangePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                originalPassword,
                "NewSecurePass@2026",
                "NewSecurePass@2026"
        );

        mockMvc.perform(post("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Password changed successfully")));

        // 1. Verify database was updated with BCrypt hash
        User updatedUser = userRepository.findById(userA.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewSecurePass@2026", updatedUser.getPasswordHash()));
        assertFalse(passwordEncoder.matches(originalPassword, updatedUser.getPasswordHash()));

        // 2. Verify login with NEW password succeeds
        LoginRequest newLogin = new LoginRequest("alice@connectly.io", "NewSecurePass@2026");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", notNullValue()));

        // 3. Verify login with OLD password fails
        LoginRequest oldLogin = new LoginRequest("alice@connectly.io", originalPassword);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Change Password: Incorrect current password -> Rejected")
    void testChangePassword_IncorrectCurrentPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongCurrentPass@999",
                "NewSecurePass@2026",
                "NewSecurePass@2026"
        );

        mockMvc.perform(post("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Current password is incorrect")));
    }

    @Test
    @DisplayName("Change Password: Non-matching confirmation -> Rejected")
    void testChangePassword_NonMatchingConfirmation() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                originalPassword,
                "NewSecurePass@2026",
                "DifferentPass@2026"
        );

        mockMvc.perform(post("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("do not match")));
    }

    @Test
    @DisplayName("Change Password: Invalid new password complexity -> Rejected")
    void testChangePassword_InvalidComplexity() throws Exception {
        // Too short (< 8 chars)
        ChangePasswordRequest tooShort = new ChangePasswordRequest(originalPassword, "Short1!", "Short1!");
        mockMvc.perform(post("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooShort)))
                .andExpect(status().isBadRequest());

        // Missing number
        ChangePasswordRequest missingNumber = new ChangePasswordRequest(originalPassword, "NoNumberPass!", "NoNumberPass!");
        mockMvc.perform(post("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingNumber)))
                .andExpect(status().isBadRequest());

        // Missing special character
        ChangePasswordRequest missingSpecial = new ChangePasswordRequest(originalPassword, "NoSpecialChar123", "NoSpecialChar123");
        mockMvc.perform(post("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingSpecial)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Account Privacy Settings: Update lastSeenPrivacy and profilePhotoPrivacy")
    void testUpdatePrivacySettings() throws Exception {
        UpdatePrivacySettingsRequest req = new UpdatePrivacySettingsRequest("My Contacts", "Nobody");

        mockMvc.perform(put("/api/v1/users/me/privacy")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastSeenPrivacy", is("My Contacts")))
                .andExpect(jsonPath("$.data.profilePhotoPrivacy", is("Nobody")));

        User updated = userRepository.findById(userA.getId()).orElseThrow();
        assertEquals("My Contacts", updated.getLastSeenPrivacy());
        assertEquals("Nobody", updated.getProfilePhotoPrivacy());
    }

    @Test
    @DisplayName("General Settings: Update bio, color accent, and display name")
    void testUpdateGeneralSettings() throws Exception {
        UpdateSettingsRequest req = new UpdateSettingsRequest();
        req.setName("Alice W. Johnson");
        req.setBio("Building scalable real-time systems with Connectly");
        req.setColorAccent("#10b981");

        mockMvc.perform(put("/api/v1/users/me/settings")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Alice W. Johnson")))
                .andExpect(jsonPath("$.data.bio", is("Building scalable real-time systems with Connectly")))
                .andExpect(jsonPath("$.data.colorAccent", is("#10b981")));
    }
}
