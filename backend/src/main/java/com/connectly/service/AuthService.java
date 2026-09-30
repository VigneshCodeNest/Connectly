package com.connectly.service;

import com.connectly.dto.request.LoginRequest;
import com.connectly.dto.request.RegisterRequest;
import com.connectly.dto.response.AuthResponse;
import com.connectly.dto.response.UserSummaryResponse;
import com.connectly.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token);
    UserSummaryResponse getCurrentUserProfile();
    User getAuthenticatedUser();
}
