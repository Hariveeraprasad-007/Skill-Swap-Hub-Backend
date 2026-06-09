package com.skillswap.experience.dto;

import com.skillswap.experience.enums.ExperienceType;
import java.time.LocalDate;
import java.util.UUID;

public record ExperienceResponse(
        UUID id, String title, String organization, String description,
        LocalDate startDate, LocalDate endDate, ExperienceType type
) {}
