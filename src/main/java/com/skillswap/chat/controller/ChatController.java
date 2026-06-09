package com.skillswap.chat.controller;

import com.skillswap.chat.dto.ChatMessageRequest;
import com.skillswap.chat.dto.ChatMessageResponse;
import com.skillswap.chat.service.ChatService;
import com.skillswap.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "1-to-1 messaging and content sharing")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/messages")
    @Operation(summary = "Send a message")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            Authentication auth, @Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent", chatService.sendMessage(auth.getName(), request)));
    }

    @GetMapping("/conversations")
    @Operation(summary = "List all conversations (latest message preview)")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversations(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Conversations retrieved", chatService.getConversations(auth.getName())));
    }

    @GetMapping("/messages/{userId}")
    @Operation(summary = "Get message history with a user (paginated)")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversation(
            Authentication auth, @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved",
                chatService.getConversation(auth.getName(), userId, page, size)));
    }

    @PutMapping("/messages/{senderId}/read")
    @Operation(summary = "Mark all messages from a sender as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(Authentication auth, @PathVariable UUID senderId) {
        chatService.markAsRead(auth.getName(), senderId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read"));
    }
}
