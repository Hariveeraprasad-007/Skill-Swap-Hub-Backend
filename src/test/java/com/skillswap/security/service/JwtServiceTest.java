package com.skillswap.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecretBase64 = "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1mb3ItaHMyNTY="; // 256-bit base64 secret

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretBase64);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L); // 15 mins
    }

    @Test
    void generateAndExtractToken_success() {
        String email = "user@test.com";
        String token = jwtService.generateToken(email);

        assertNotNull(token);
        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void isTokenValid_success() {
        String email = "user@test.com";
        String token = jwtService.generateToken(email);

        UserDetails userDetails = new User(email, "password", Collections.emptyList());

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_fail_wrongUser() {
        String email = "user@test.com";
        String token = jwtService.generateToken(email);

        UserDetails userDetails = new User("other@test.com", "password", Collections.emptyList());

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void generateToken_withExtraClaims_success() {
        String email = "user@test.com";
        java.util.Map<String, Object> extraClaims = java.util.Map.of("role", "ADMIN", "customId", 123);
        String token = jwtService.generateToken(email, extraClaims);

        assertNotNull(token);
        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void isTokenValid_fail_expired() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -5000L); // expired
        String email = "user@test.com";
        String token = jwtService.generateToken(email);

        UserDetails userDetails = new User(email, "password", Collections.emptyList());

        assertThrows(Exception.class, () -> jwtService.isTokenValid(token, userDetails));
    }
}
