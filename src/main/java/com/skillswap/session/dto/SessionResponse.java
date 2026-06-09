package com.skillswap.session.dto;

import com.skillswap.session.enums.BillingType;
import com.skillswap.session.enums.SessionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID teacherId,
        String teacherName,
        UUID studentId,
        String studentName,
        String skillName,
        BillingType billingType,
        BigDecimal price,
        UUID swapSessionId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SessionStatus status,
        String virtualRoomUrl,
        String cancelReason,
        LocalDateTime createdAt
) {}
