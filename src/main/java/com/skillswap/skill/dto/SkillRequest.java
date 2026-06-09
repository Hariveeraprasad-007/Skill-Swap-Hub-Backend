package com.skillswap.skill.dto;

import com.skillswap.skill.enums.PreferredMode;
import com.skillswap.skill.enums.ProficiencyLevel;
import com.skillswap.skill.enums.SkillDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SkillRequest(
        @NotBlank(message = "Skill name is required")
        String skillName,

        @NotNull(message = "Proficiency level is required")
        ProficiencyLevel proficiency,

        @NotNull(message = "Direction (TEACH or LEARN) is required")
        SkillDirection direction,

        PreferredMode preferredMode,

        BigDecimal hourlyRate
) {}
