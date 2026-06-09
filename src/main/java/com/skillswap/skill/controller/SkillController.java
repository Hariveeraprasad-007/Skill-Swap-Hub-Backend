package com.skillswap.skill.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.skill.dto.SkillRequest;
import com.skillswap.skill.dto.SkillResponse;
import com.skillswap.skill.service.SkillService;
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
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Manage your teaching and learning skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    @Operation(summary = "Add a new skill")
    public ResponseEntity<ApiResponse<SkillResponse>> addSkill(
            Authentication auth, @Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.addSkill(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a skill")
    public ResponseEntity<ApiResponse<SkillResponse>> updateSkill(
            Authentication auth, @PathVariable UUID id, @Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.updateSkill(auth.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Skill updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a skill")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(Authentication auth, @PathVariable UUID id) {
        skillService.deleteSkill(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted"));
    }

    @GetMapping("/my")
    @Operation(summary = "List your skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getMySkills(Authentication auth) {
        List<SkillResponse> skills = skillService.getMySkills(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Skills retrieved", skills));
    }
}
