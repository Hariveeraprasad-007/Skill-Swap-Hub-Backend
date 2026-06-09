package com.skillswap.chat.websocket;

import com.skillswap.chat.dto.ChatMessageRequest;
import com.skillswap.chat.dto.ChatMessageResponse;
import com.skillswap.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Handles real-time WebSocket messages via STOMP.
 *
 * Send to:    /app/chat.send
 * Receive at: /topic/messages/{userId}
 */
@Controller
public class ChatWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatWebSocketHandler(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        // Persist the message
        ChatMessageResponse response = chatService.sendMessage(principal.getName(), request);

        // Push to receiver's personal topic
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.receiverId(), response);
    }
}
