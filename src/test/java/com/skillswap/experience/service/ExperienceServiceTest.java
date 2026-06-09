package com.skillswap.experience.service;

import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.experience.dto.ExperienceRequest;
import com.skillswap.experience.dto.ExperienceResponse;
import com.skillswap.experience.entity.Experience;
import com.skillswap.experience.enums.ExperienceType;
import com.skillswap.experience.repository.ExperienceRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExperienceService experienceService;

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
    void addExperience_success() {
        ExperienceRequest request = new ExperienceRequest(
                "Software Engineer", "Google", "Built search engines",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), ExperienceType.PROFESSIONAL
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Experience saved = new Experience();
        saved.setId(UUID.randomUUID());
        saved.setUser(user);
        saved.setTitle("Software Engineer");
        saved.setOrganization("Google");
        saved.setDescription("Built search engines");
        saved.setStartDate(LocalDate.of(2025, 1, 1));
        saved.setEndDate(LocalDate.of(2026, 1, 1));
        saved.setType(ExperienceType.PROFESSIONAL);

        when(experienceRepository.save(any(Experience.class))).thenReturn(saved);

        ExperienceResponse response = experienceService.add(user.getEmail(), request);

        assertNotNull(response);
        assertEquals("Software Engineer", response.title());
        assertEquals("Google", response.organization());
        assertEquals(ExperienceType.PROFESSIONAL, response.type());
    }

    @Test
    void addExperience_userNotFound() {
        ExperienceRequest request = new ExperienceRequest(
                "Software Engineer", "Google", "Built search engines",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), ExperienceType.PROFESSIONAL
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                experienceService.add("unknown@test.com", request)
        );
    }

    @Test
    void updateExperience_success() {
        UUID expId = UUID.randomUUID();
        ExperienceRequest request = new ExperienceRequest(
                "Lead Engineer", "Google", "Managed search engines",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 1), ExperienceType.PROFESSIONAL
        );

        Experience existing = new Experience();
        existing.setId(expId);
        existing.setUser(user);
        existing.setTitle("Software Engineer");
        existing.setOrganization("Google");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(experienceRepository.findById(expId)).thenReturn(Optional.of(existing));
        when(experienceRepository.save(any(Experience.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExperienceResponse response = experienceService.update(user.getEmail(), expId, request);

        assertNotNull(response);
        assertEquals("Lead Engineer", response.title());
        assertEquals("Managed search engines", response.description());
    }

    @Test
    void updateExperience_userNotFound() {
        UUID expId = UUID.randomUUID();
        ExperienceRequest request = new ExperienceRequest(
                "Lead Engineer", "Google", "Managed search engines",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 1), ExperienceType.PROFESSIONAL
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                experienceService.update("unknown@test.com", expId, request)
        );
    }

    @Test
    void updateExperience_notFound() {
        UUID expId = UUID.randomUUID();
        ExperienceRequest request = new ExperienceRequest(
                "Lead Engineer", "Google", "Managed search engines",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 1), ExperienceType.PROFESSIONAL
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(experienceRepository.findById(expId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                experienceService.update(user.getEmail(), expId, request)
        );
    }

    @Test
    void updateExperience_fail_wrongUser() {
        UUID expId = UUID.randomUUID();
        ExperienceRequest request = new ExperienceRequest(
                "Lead Engineer", "Google", "Managed search engines",
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 1), ExperienceType.PROFESSIONAL
        );

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        Experience existing = new Experience();
        existing.setId(expId);
        existing.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(experienceRepository.findById(expId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                experienceService.update(user.getEmail(), expId, request)
        );
    }

    @Test
    void deleteExperience_success() {
        UUID expId = UUID.randomUUID();
        Experience existing = new Experience();
        existing.setId(expId);
        existing.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(experienceRepository.findById(expId)).thenReturn(Optional.of(existing));
        doNothing().when(experienceRepository).delete(existing);

        experienceService.delete(user.getEmail(), expId);

        verify(experienceRepository, times(1)).delete(existing);
    }

    @Test
    void deleteExperience_userNotFound() {
        UUID expId = UUID.randomUUID();
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                experienceService.delete("unknown@test.com", expId)
        );
    }

    @Test
    void deleteExperience_notFound() {
        UUID expId = UUID.randomUUID();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(experienceRepository.findById(expId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                experienceService.delete(user.getEmail(), expId)
        );
    }

    @Test
    void deleteExperience_fail_wrongUser() {
        UUID expId = UUID.randomUUID();
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        Experience existing = new Experience();
        existing.setId(expId);
        existing.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(experienceRepository.findById(expId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                experienceService.delete(user.getEmail(), expId)
        );
    }

    @Test
    void getMyExperiences_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Experience exp = new Experience();
        exp.setId(UUID.randomUUID());
        exp.setUser(user);
        exp.setTitle("Developer");
        exp.setOrganization("GitHub");
        exp.setType(ExperienceType.PROJECT);

        when(experienceRepository.findByUserId(userId)).thenReturn(List.of(exp));

        List<ExperienceResponse> results = experienceService.getMyExperiences(user.getEmail());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Developer", results.get(0).title());
    }

    @Test
    void getMyExperiences_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                experienceService.getMyExperiences("unknown@test.com")
        );
    }
}
