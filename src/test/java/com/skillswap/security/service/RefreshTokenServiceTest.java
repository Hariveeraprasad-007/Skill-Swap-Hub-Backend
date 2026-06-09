package com.skillswap.security.service;

import com.skillswap.security.entity.RefreshToken;
import com.skillswap.security.repository.RefreshTokenRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");

        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604800000L); // 7 days
    }

    @Test
    void createRefreshToken_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenRepository).deleteByUserId(userId);

        RefreshToken savedToken = new RefreshToken();
        savedToken.setUser(user);
        savedToken.setToken("some-uuid-token");
        savedToken.setExpiryDate(Instant.now().plusSeconds(600));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = refreshTokenService.createRefreshToken(userId);

        assertNotNull(result);
        assertEquals("some-uuid-token", result.getToken());
        verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void verifyRefreshToken_success() {
        RefreshToken token = new RefreshToken();
        token.setToken("some-token");
        token.setExpiryDate(Instant.now().plusSeconds(600));

        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.verifyRefreshToken("some-token");

        assertNotNull(result);
        assertEquals("some-token", result.getToken());
    }

    @Test
    void verifyRefreshToken_expired() {
        RefreshToken token = new RefreshToken();
        token.setToken("some-token");
        token.setExpiryDate(Instant.now().minusSeconds(600)); // expired in the past

        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(token));
        doNothing().when(refreshTokenRepository).delete(token);

        assertThrows(IllegalArgumentException.class, () ->
                refreshTokenService.verifyRefreshToken("some-token")
        );
        verify(refreshTokenRepository, times(1)).delete(token);
    }
}
