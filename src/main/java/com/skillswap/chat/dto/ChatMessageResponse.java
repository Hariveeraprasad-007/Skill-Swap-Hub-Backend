package com.skillswap.chat.dto;

import com.skillswap.chat.enums.MessageType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id, UUID senderId, String senderName, UUID receiverId, String receiverName,
        String content, String attachmentUrl, MessageType messageType, boolean isRead, LocalDateTime sentAt
) {}
