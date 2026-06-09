package com.skillswap.experience.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.experience.dto.ExperienceRequest;
import com.skillswap.experience.dto.ExperienceResponse;
import com.skillswap.experience.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/experiences")
@Tag(name = "Experiences", description = "Manage your experience, projects, and achievements")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping
    @Operation(summary = "Add an experience")
    public ResponseEntity<ApiResponse<ExperienceResponse>> add(
            Authentication auth, @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Experience added", experienceService.add(auth.getName(), request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an experience")
    public ResponseEntity<ApiResponse<ExperienceResponse>> update(
            Authentication auth, @PathVariable UUID id, @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Experience updated", experienceService.update(auth.getName(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an experience")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable UUID id) {
        experienceService.delete(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Experience deleted"));
    }

    @GetMapping("/my")
    @Operation(summary = "List your experiences")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> getMyExperiences(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Experiences retrieved", experienceService.getMyExperiences(auth.getName())));
    }
}
