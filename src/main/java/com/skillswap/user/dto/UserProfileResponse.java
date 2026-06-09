package com.skillswap.user.dto;

import com.skillswap.skill.dto.SkillResponse;
import com.skillswap.certification.dto.CertificationResponse;
import com.skillswap.experience.dto.ExperienceResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String bio,
        String profilePictureUrl,
        String githubUrl,
        String linkedinUrl,
        String leetcodeUrl,
        String codeforcesUrl,
        String role,
        double averageRating,
        int totalSessions,
        List<SkillResponse> skills,
        List<CertificationResponse> certifications,
        List<ExperienceResponse> experiences,
        LocalDateTime createdAt
) {}
