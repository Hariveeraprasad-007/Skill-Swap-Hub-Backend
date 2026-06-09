package com.skillswap.skill.dto;

import com.skillswap.skill.enums.PreferredMode;
import com.skillswap.skill.enums.ProficiencyLevel;
import com.skillswap.skill.enums.SkillDirection;

import java.math.BigDecimal;
import java.util.UUID;

public record SkillResponse(
        UUID id,
        String skillName,
        ProficiencyLevel proficiency,
        SkillDirection direction,
        PreferredMode preferredMode,
        BigDecimal hourlyRate
) {}
