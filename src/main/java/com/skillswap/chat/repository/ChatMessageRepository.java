package com.skillswap.chat.repository;

import com.skillswap.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.sentAt DESC
    """)
    Page<ChatMessage> findConversation(@Param("userId1") UUID userId1,
                                       @Param("userId2") UUID userId2,
                                       Pageable pageable);

    /**
     * Get latest message for each conversation partner (for conversation list).
     */
    @Query(value = """
        SELECT DISTINCT ON (partner_id) *
        FROM (
            SELECT *, CASE
                WHEN sender_id = :userId THEN receiver_id
                ELSE sender_id
            END AS partner_id
            FROM chat_messages
            WHERE sender_id = :userId OR receiver_id = :userId
        ) sub
        ORDER BY partner_id, sent_at DESC
    """, nativeQuery = true)
    List<ChatMessage> findConversationPreviews(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markAllAsRead(@Param("senderId") UUID senderId, @Param("receiverId") UUID receiverId);
}
