package com.skillswap.chat.dto;

import com.skillswap.chat.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChatMessageRequest(
        @NotNull UUID receiverId,
        @NotBlank String content,
        String attachmentUrl,
        MessageType messageType
) {}
