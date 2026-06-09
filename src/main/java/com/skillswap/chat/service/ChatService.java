package com.skillswap.chat.service;

import com.skillswap.chat.dto.ChatMessageRequest;
import com.skillswap.chat.dto.ChatMessageResponse;
import com.skillswap.chat.entity.ChatMessage;
import com.skillswap.chat.enums.MessageType;
import com.skillswap.chat.repository.ChatMessageRepository;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessageResponse sendMessage(String senderEmail, ChatMessageRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send message to yourself");
        }

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.content());
        message.setAttachmentUrl(request.attachmentUrl());
        message.setMessageType(request.messageType() != null ? request.messageType() : MessageType.TEXT);

        message = chatMessageRepository.save(message);
        return toResponse(message);
    }

    public List<ChatMessageResponse> getConversation(String email, UUID otherUserId, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return chatMessageRepository.findConversation(
                        user.getId(), otherUserId, PageRequest.of(page, size))
                .stream().map(this::toResponse).toList();
    }

    public List<ChatMessageResponse> getConversations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return chatMessageRepository.findConversationPreviews(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void markAsRead(String email, UUID senderId) {
        User receiver = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        chatMessageRepository.markAllAsRead(senderId, receiver.getId());
    }

    private ChatMessageResponse toResponse(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(), m.getSender().getId(),
                m.getSender().getFirstName() + " " + m.getSender().getLastName(),
                m.getReceiver().getId(),
                m.getReceiver().getFirstName() + " " + m.getReceiver().getLastName(),
                m.getContent(), m.getAttachmentUrl(), m.getMessageType(), m.isRead(), m.getSentAt()
        );
    }
}
