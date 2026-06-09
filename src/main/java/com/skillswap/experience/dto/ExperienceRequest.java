package com.skillswap.experience.dto;

import com.skillswap.experience.enums.ExperienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ExperienceRequest(
        @NotBlank(message = "Title is required") String title,
        String organization,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        @NotNull(message = "Type is required") ExperienceType type
) {}
