package com.skillswap.user.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.user.dto.UserProfileResponse;
import com.skillswap.user.dto.UserProfileUpdateRequest;
import com.skillswap.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profiles and discovery")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get your full profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", userService.getMyProfile(auth.getName())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update your profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication auth, @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(auth.getName(), request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's public profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", userService.getUserProfile(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by skill and direction")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchBySkill(
            @RequestParam String skill,
            @RequestParam(defaultValue = "TEACH") SkillDirection direction) {
        return ResponseEntity.ok(ApiResponse.success("Users found", userService.searchBySkill(skill, direction)));
    }
}
