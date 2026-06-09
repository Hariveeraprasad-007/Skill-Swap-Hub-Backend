package com.skillswap.report.dto;

import com.skillswap.report.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponse(
        UUID id, UUID reporterId, String reporterName,
        UUID reportedUserId, String reportedUserName,
        String reason, String description, ReportStatus status, LocalDateTime createdAt
) {}
