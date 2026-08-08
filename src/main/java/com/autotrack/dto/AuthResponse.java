package com.autotrack.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UserSummary user
) {}
