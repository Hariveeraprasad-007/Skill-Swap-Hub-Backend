package com.skillswap.security.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String firstName,
        String lastName,
        String role
) {}
