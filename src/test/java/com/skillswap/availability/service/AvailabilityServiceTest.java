package com.skillswap.availability.service;

import com.skillswap.availability.dto.AvailabilityRequest;
import com.skillswap.availability.dto.AvailabilityResponse;
import com.skillswap.availability.entity.Availability;
import com.skillswap.availability.repository.AvailabilityRepository;
import com.skillswap.common.exception.DuplicateResourceException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

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
    void addAvailability_success() {
        AvailabilityRequest request = new AvailabilityRequest(
                1, LocalTime.of(9, 0), LocalTime.of(12, 0), "UTC"
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(availabilityRepository.findOverlapping(userId, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)))
                .thenReturn(Collections.emptyList());

        Availability saved = new Availability();
        saved.setId(UUID.randomUUID());
        saved.setUser(user);
        saved.setDayOfWeek(1);
        saved.setStartTime(LocalTime.of(9, 0));
        saved.setEndTime(LocalTime.of(12, 0));
        saved.setTimezone("UTC");

        when(availabilityRepository.save(any(Availability.class))).thenReturn(saved);

        AvailabilityResponse response = availabilityService.add(user.getEmail(), request);

        assertNotNull(response);
        assertEquals(1, response.dayOfWeek());
        assertEquals(LocalTime.of(9, 0), response.startTime());
        assertEquals(LocalTime.of(12, 0), response.endTime());
        verify(availabilityRepository, times(1)).save(any(Availability.class));
    }

    @Test
    void addAvailability_fail_invalidTimeRange() {
        AvailabilityRequest request = new AvailabilityRequest(
                1, LocalTime.of(12, 0), LocalTime.of(9, 0), "UTC"
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                availabilityService.add(user.getEmail(), request)
        );
    }

    @Test
    void addAvailability_fail_overlap() {
        AvailabilityRequest request = new AvailabilityRequest(
                1, LocalTime.of(9, 0), LocalTime.of(12, 0), "UTC"
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Availability overlapping = new Availability();
        overlapping.setId(UUID.randomUUID());
        overlapping.setUser(user);
        overlapping.setDayOfWeek(1);
        overlapping.setStartTime(LocalTime.of(10, 0));
        overlapping.setEndTime(LocalTime.of(11, 0));

        when(availabilityRepository.findOverlapping(userId, 1, LocalTime.of(9, 0), LocalTime.of(12, 0)))
                .thenReturn(List.of(overlapping));

        assertThrows(DuplicateResourceException.class, () ->
                availabilityService.add(user.getEmail(), request)
        );
    }
}
