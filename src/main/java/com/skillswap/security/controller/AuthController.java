package com.skillswap.security.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.common.exception.DuplicateResourceException;
import com.skillswap.security.dto.*;
import com.skillswap.security.entity.RefreshToken;
import com.skillswap.security.service.GoogleOAuthService;
import com.skillswap.security.service.JwtService;
import com.skillswap.security.service.RefreshTokenService;
import com.skillswap.user.entity.User;
import com.skillswap.user.enums.AuthProvider;
import com.skillswap.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, Google sign-in, and token refresh")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final GoogleOAuthService googleOAuthService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          GoogleOAuthService googleOAuthService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthResponse authResponse = new AuthResponse(
                accessToken, refreshToken.getToken(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthResponse authResponse = new AuthResponse(
                accessToken, refreshToken.getToken(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole().name()
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/google")
    @Operation(summary = "Sign in with Google ID token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleSignIn(@Valid @RequestBody GoogleAuthRequest request) {
        User user = googleOAuthService.authenticateWithGoogle(request.idToken());

        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthResponse authResponse = new AuthResponse(
                accessToken, refreshToken.getToken(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole().name()
        );

        return ResponseEntity.ok(ApiResponse.success("Google sign-in successful", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.refreshToken());
        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(user.getEmail());

        AuthResponse authResponse = new AuthResponse(
                accessToken, refreshToken.getToken(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole().name()
        );

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.refreshToken());
        refreshTokenService.deleteByUserId(refreshToken.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
