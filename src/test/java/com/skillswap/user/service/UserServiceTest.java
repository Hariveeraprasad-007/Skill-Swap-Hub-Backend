package com.skillswap.user.service;

import com.skillswap.certification.repository.CertificationRepository;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.experience.repository.ExperienceRepository;
import com.skillswap.skill.entity.UserSkill;
import com.skillswap.skill.enums.PreferredMode;
import com.skillswap.skill.enums.ProficiencyLevel;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.skill.repository.UserSkillRepository;
import com.skillswap.user.dto.UserProfileResponse;
import com.skillswap.user.dto.UserProfileUpdateRequest;
import com.skillswap.user.entity.User;
import com.skillswap.user.enums.Role;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSkillRepository skillRepository;

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private UserService userService;

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
        user.setRole(Role.USER);
        user.setBio("Hello world");
    }

    @Test
    void getMyProfile_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(experienceRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        UserProfileResponse response = userService.getMyProfile(user.getEmail());

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
        assertEquals("Alice", response.firstName());
        assertEquals("Wonderland", response.lastName());
        assertEquals("USER", response.role());
    }

    @Test
    void getMyProfile_notFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.getMyProfile("unknown@test.com")
        );
    }

    @Test
    void updateProfile_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skillRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(experienceRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "UpdatedFirst", "UpdatedLast", "New bio", "new-pic.jpg",
                "new-github", "new-linkedin", "new-leetcode", "new-codeforces"
        );

        UserProfileResponse response = userService.updateProfile(user.getEmail(), request);

        assertNotNull(response);
        assertEquals("UpdatedFirst", response.firstName());
        assertEquals("UpdatedLast", response.lastName());
        assertEquals("New bio", response.bio());
        assertEquals("new-pic.jpg", response.profilePictureUrl());
        assertEquals("new-github", response.githubUrl());
        assertEquals("new-linkedin", response.linkedinUrl());
        assertEquals("new-leetcode", response.leetcodeUrl());
        assertEquals("new-codeforces", response.codeforcesUrl());
    }

    @Test
    void searchBySkill_success() {
        when(userRepository.searchBySkill("Java", SkillDirection.TEACH)).thenReturn(List.of(user));
        when(skillRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(certificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(experienceRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<UserProfileResponse> results = userService.searchBySkill("Java", SkillDirection.TEACH);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(user.getEmail(), results.get(0).email());
    }
}
