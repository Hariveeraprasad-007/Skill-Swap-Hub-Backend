package com.skillswap.chat.service;

import com.skillswap.chat.dto.ChatMessageRequest;
import com.skillswap.chat.dto.ChatMessageResponse;
import com.skillswap.chat.entity.ChatMessage;
import com.skillswap.chat.enums.MessageType;
import com.skillswap.chat.repository.ChatMessageRepository;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User sender;
    private User receiver;
    private UUID senderId;
    private UUID receiverId;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        sender = new User();
        sender.setId(senderId);
        sender.setEmail("sender@test.com");
        sender.setFirstName("Sender");
        sender.setLastName("User");

        receiver = new User();
        receiver.setId(receiverId);
        receiver.setEmail("receiver@test.com");
        receiver.setFirstName("Receiver");
        receiver.setLastName("User");
    }

    @Test
    void sendMessage_success() {
        ChatMessageRequest request = new ChatMessageRequest(receiverId, "Hello", null, MessageType.TEXT);

        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        ChatMessage saved = new ChatMessage();
        saved.setId(UUID.randomUUID());
        saved.setSender(sender);
        saved.setReceiver(receiver);
        saved.setContent("Hello");
        saved.setMessageType(MessageType.TEXT);

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessageResponse response = chatService.sendMessage(sender.getEmail(), request);

        assertNotNull(response);
        assertEquals("Hello", response.content());
        assertEquals(MessageType.TEXT, response.messageType());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_fail_selfMessage() {
        ChatMessageRequest request = new ChatMessageRequest(senderId, "Hello to myself", null, MessageType.TEXT);

        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));

        assertThrows(IllegalArgumentException.class, () ->
                chatService.sendMessage(sender.getEmail(), request)
        );
    }

    @Test
    void sendMessage_fail_senderNotFound() {
        ChatMessageRequest request = new ChatMessageRequest(receiverId, "Hello", null, MessageType.TEXT);
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatService.sendMessage("unknown@test.com", request)
        );
    }

    @Test
    void sendMessage_fail_receiverNotFound() {
        ChatMessageRequest request = new ChatMessageRequest(receiverId, "Hello", null, MessageType.TEXT);
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatService.sendMessage(sender.getEmail(), request)
        );
    }

    @Test
    void sendMessage_defaultMessageType() {
        ChatMessageRequest request = new ChatMessageRequest(receiverId, "Hello", null, null); // message type is null

        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        ChatMessage saved = new ChatMessage();
        saved.setId(UUID.randomUUID());
        saved.setSender(sender);
        saved.setReceiver(receiver);
        saved.setContent("Hello");
        saved.setMessageType(MessageType.TEXT); // should default to TEXT

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessageResponse response = chatService.sendMessage(sender.getEmail(), request);

        assertNotNull(response);
        assertEquals(MessageType.TEXT, response.messageType());
    }

    @Test
    void getConversation_success() {
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        ChatMessage msg = new ChatMessage();
        msg.setId(UUID.randomUUID());
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent("Hey");
        msg.setMessageType(MessageType.TEXT);

        @SuppressWarnings("unchecked")
        org.springframework.data.domain.Page<ChatMessage> page = mock(org.springframework.data.domain.Page.class);
        when(page.stream()).thenReturn(java.util.stream.Stream.of(msg));

        when(chatMessageRepository.findConversation(eq(senderId), eq(receiverId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        java.util.List<ChatMessageResponse> results = chatService.getConversation(sender.getEmail(), receiverId, 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Hey", results.get(0).content());
    }

    @Test
    void getConversation_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatService.getConversation("unknown@test.com", receiverId, 0, 10)
        );
    }

    @Test
    void getConversations_success() {
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        ChatMessage msg = new ChatMessage();
        msg.setId(UUID.randomUUID());
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent("Last message");
        msg.setMessageType(MessageType.TEXT);

        when(chatMessageRepository.findConversationPreviews(senderId)).thenReturn(java.util.List.of(msg));

        java.util.List<ChatMessageResponse> results = chatService.getConversations(sender.getEmail());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Last message", results.get(0).content());
    }

    @Test
    void getConversations_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatService.getConversations("unknown@test.com")
        );
    }

    @Test
    void markAsRead_success() {
        when(userRepository.findByEmail(receiver.getEmail())).thenReturn(Optional.of(receiver));
        doNothing().when(chatMessageRepository).markAllAsRead(senderId, receiverId);

        chatService.markAsRead(receiver.getEmail(), senderId);

        verify(chatMessageRepository, times(1)).markAllAsRead(senderId, receiverId);
    }

    @Test
    void markAsRead_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatService.markAsRead("unknown@test.com", senderId)
        );
    }
}
