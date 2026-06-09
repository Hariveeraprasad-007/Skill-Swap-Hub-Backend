package com.skillswap.notification.service;

import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.notification.dto.NotificationResponse;
import com.skillswap.notification.entity.Notification;
import com.skillswap.notification.enums.NotificationType;
import com.skillswap.notification.repository.NotificationRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setFirstName("Alice");
        user.setLastName("Wonderland");
    }

    @Test
    void sendNotification_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Notification mockSaved = new Notification();
        mockSaved.setId(UUID.randomUUID());
        mockSaved.setUser(user);
        mockSaved.setTitle("Test Title");
        mockSaved.setMessage("Test Message");
        mockSaved.setType(NotificationType.SESSION_REQUEST);
        mockSaved.setReferenceId("ref-123");
        mockSaved.setRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(mockSaved);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/notifications/" + userId), any(NotificationResponse.class));

        notificationService.sendNotification(userId, "Test Title", "Test Message", NotificationType.SESSION_REQUEST, "ref-123");

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/" + userId), any(NotificationResponse.class));
    }

    @Test
    void sendNotification_fail_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.sendNotification(userId, "Title", "Msg", NotificationType.SESSION_REQUEST, "ref")
        );
    }

    @Test
    void getNotifications_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUser(user);
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setType(NotificationType.SESSION_ACCEPTED);

        @SuppressWarnings("unchecked")
        org.springframework.data.domain.Page<Notification> page = mock(org.springframework.data.domain.Page.class);
        when(page.stream()).thenReturn(java.util.stream.Stream.of(notification));

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        List<NotificationResponse> results = notificationService.getNotifications(user.getEmail(), 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Title", results.get(0).title());
    }

    @Test
    void getNotifications_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.getNotifications("unknown@test.com", 0, 10)
        );
    }

    @Test
    void getUnreadCount_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(notificationRepository.countUnread(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(user.getEmail());

        assertEquals(5L, count);
    }

    @Test
    void getUnreadCount_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.getUnreadCount("unknown@test.com")
        );
    }

    @Test
    void markAsRead_success() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.markAsRead(notificationId);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markAsRead_fail_notFound() {
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.markAsRead(notificationId)
        );
    }

    @Test
    void markAllAsRead_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(notificationRepository).markAllAsRead(userId);

        notificationService.markAllAsRead(user.getEmail());

        verify(notificationRepository, times(1)).markAllAsRead(userId);
    }

    @Test
    void markAllAsRead_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.markAllAsRead("unknown@test.com")
        );
    }
}
