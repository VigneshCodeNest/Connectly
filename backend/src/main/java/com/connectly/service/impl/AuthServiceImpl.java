package com.connectly.service.impl;

import com.connectly.dto.request.LoginRequest;
import com.connectly.dto.request.RegisterRequest;
import com.connectly.dto.response.AuthResponse;
import com.connectly.dto.response.UserSummaryResponse;
import com.connectly.entity.User;
import com.connectly.exception.BadRequestException;
import com.connectly.exception.UnauthorizedException;
import com.connectly.repository.UserRepository;
import com.connectly.security.JwtTokenProvider;
import com.connectly.security.UserPrincipal;
import com.connectly.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        String cleanUsername = request.getUsername().trim();
        if (!cleanUsername.startsWith("@")) {
            cleanUsername = "@" + cleanUsername;
        }

        // Duplicate checks
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new BadRequestException("An account with this email address already exists");
        }
        if (userRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new BadRequestException("Username " + cleanUsername + " is already taken");
        }

        // Create user with BCrypt hashed password
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(cleanEmail);
        user.setUsername(cleanUsername);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAvatarUrl(request.getAvatarUrl());
        user.setBio(request.getBio() != null ? request.getBio().trim() : "Hey there! I am using Connectly");
        user.setColorAccent("#6366f1");
        user.setOnline(true);
        user.setLastSeenPrivacy("Everyone");
        user.setProfilePhotoPrivacy("Everyone");

        User savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

        return new AuthResponse(
                token,
                jwtTokenProvider.getExpirationMs(),
                UserSummaryResponse.fromEntity(savedUser)
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmail().trim();

        User user = userRepository.findByEmailIgnoreCase(identifier)
                .or(() -> userRepository.findByUsernameIgnoreCase(identifier.startsWith("@") ? identifier : "@" + identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        user.setOnline(true);
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(
                token,
                jwtTokenProvider.getExpirationMs(),
                UserSummaryResponse.fromEntity(user)
        );
    }

    @Override
    @Transactional
    public void logout(String token) {
        try {
            User user = getAuthenticatedUser();
            if (user != null) {
                user.setOnline(false);
                userRepository.save(user);
            }
        } catch (Exception ignored) {
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUserProfile() {
        User user = getAuthenticatedUser();
        return UserSummaryResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user no longer exists"));
    }
}
