package com.skillswap.user.dto;

public record UserProfileUpdateRequest(
        String firstName,
        String lastName,
        String bio,
        String profilePictureUrl,
        String githubUrl,
        String linkedinUrl,
        String leetcodeUrl,
        String codeforcesUrl
) {}
