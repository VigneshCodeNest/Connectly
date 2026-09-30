package com.connectly.dto.response;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private UserSummaryResponse user;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresInMs, UserSummaryResponse user) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresInMs = expiresInMs;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }

    public UserSummaryResponse getUser() { return user; }
    public void setUser(UserSummaryResponse user) { this.user = user; }
}
