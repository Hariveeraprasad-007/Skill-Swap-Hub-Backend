package com.skillswap.session.dto;

import com.skillswap.session.enums.BillingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionBookRequest(
        @NotNull(message = "Teacher ID is required") UUID teacherId,
        @NotBlank(message = "Skill name is required") String skillName,
        @NotNull(message = "Billing type is required") BillingType billingType,
        BigDecimal price,
        UUID swapSessionId,
        @NotNull(message = "Start time is required") LocalDateTime startTime,
        @NotNull(message = "End time is required") LocalDateTime endTime
) {}
