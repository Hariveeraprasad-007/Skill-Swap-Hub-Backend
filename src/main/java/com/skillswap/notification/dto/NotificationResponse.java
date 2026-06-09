package com.skillswap.notification.dto;

import com.skillswap.notification.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id, String title, String message, NotificationType type,
        String referenceId, boolean isRead, LocalDateTime createdAt
) {}
