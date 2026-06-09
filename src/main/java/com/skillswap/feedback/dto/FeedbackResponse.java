package com.skillswap.feedback.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackResponse(
        UUID id, UUID sessionId, UUID reviewerId, String reviewerName,
        UUID revieweeId, String revieweeName, int rating, String comment, LocalDateTime createdAt
) {}
