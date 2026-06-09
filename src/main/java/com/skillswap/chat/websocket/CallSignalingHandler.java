package com.skillswap.chat.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebRTC signaling over WebSocket for voice/video calls.
 *
 * Signals: call-offer, call-answer, ice-candidate, call-end
 * Send to:    /app/call.signal
 * Receive at: /topic/call/{userId}
 */
@Controller
public class CallSignalingHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public CallSignalingHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/call.signal")
    public void handleSignal(@Payload Map<String, Object> signal) {
        String targetUserId = (String) signal.get("targetUserId");
        // Forward the signal to the target user's call topic
        messagingTemplate.convertAndSend("/topic/call/" + targetUserId, signal);
    }
}
