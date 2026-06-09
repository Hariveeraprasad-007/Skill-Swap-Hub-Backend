package com.skillswap.skill.service;

import com.skillswap.common.exception.DuplicateResourceException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.skill.dto.SkillRequest;
import com.skillswap.skill.dto.SkillResponse;
import com.skillswap.skill.entity.UserSkill;
import com.skillswap.skill.enums.PreferredMode;
import com.skillswap.skill.enums.ProficiencyLevel;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.skill.repository.UserSkillRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private UserSkillRepository skillRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SkillService skillService;

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
    void addSkill_success() {
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.INTERMEDIATE, SkillDirection.TEACH, PreferredMode.FREE, BigDecimal.valueOf(25.0)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(userId, "Java", SkillDirection.TEACH))
                .thenReturn(false);

        UserSkill savedSkill = new UserSkill();
        savedSkill.setId(UUID.randomUUID());
        savedSkill.setUser(user);
        savedSkill.setSkillName("Java");
        savedSkill.setProficiency(ProficiencyLevel.INTERMEDIATE);
        savedSkill.setDirection(SkillDirection.TEACH);
        savedSkill.setPreferredMode(PreferredMode.FREE);
        savedSkill.setHourlyRate(BigDecimal.valueOf(25.0));

        when(skillRepository.save(any(UserSkill.class))).thenReturn(savedSkill);

        SkillResponse response = skillService.addSkill(user.getEmail(), request);

        assertNotNull(response);
        assertEquals("Java", response.skillName());
        assertEquals(ProficiencyLevel.INTERMEDIATE, response.proficiency());
        assertEquals(SkillDirection.TEACH, response.direction());
        assertEquals(PreferredMode.FREE, response.preferredMode());
        assertEquals(BigDecimal.valueOf(25.0), response.hourlyRate());
    }

    @Test
    void addSkill_userNotFound() {
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.INTERMEDIATE, SkillDirection.TEACH, PreferredMode.FREE, BigDecimal.valueOf(25.0)
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.addSkill("unknown@test.com", request)
        );
    }

    @Test
    void addSkill_fail_duplicate() {
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.INTERMEDIATE, SkillDirection.TEACH, PreferredMode.FREE, BigDecimal.valueOf(25.0)
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(userId, "Java", SkillDirection.TEACH))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                skillService.addSkill(user.getEmail(), request)
        );
    }

    @Test
    void updateSkill_success() {
        UUID skillId = UUID.randomUUID();
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.ADVANCED, SkillDirection.TEACH, null, BigDecimal.valueOf(30.0) // null preferredMode defaults to ANY
        );

        UserSkill existingSkill = new UserSkill();
        existingSkill.setId(skillId);
        existingSkill.setUser(user);
        existingSkill.setSkillName("Java");
        existingSkill.setProficiency(ProficiencyLevel.INTERMEDIATE);
        existingSkill.setDirection(SkillDirection.TEACH);
        existingSkill.setPreferredMode(PreferredMode.FREE);
        existingSkill.setHourlyRate(BigDecimal.valueOf(25.0));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(existingSkill));
        when(skillRepository.save(any(UserSkill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SkillResponse response = skillService.updateSkill(user.getEmail(), skillId, request);

        assertNotNull(response);
        assertEquals(ProficiencyLevel.ADVANCED, response.proficiency());
        assertEquals(PreferredMode.ANY, response.preferredMode()); // check defaulting
        assertEquals(BigDecimal.valueOf(30.0), response.hourlyRate());
    }

    @Test
    void updateSkill_userNotFound() {
        UUID skillId = UUID.randomUUID();
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.ADVANCED, SkillDirection.TEACH, PreferredMode.FREE, BigDecimal.valueOf(30.0)
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.updateSkill("unknown@test.com", skillId, request)
        );
    }

    @Test
    void updateSkill_skillNotFound() {
        UUID skillId = UUID.randomUUID();
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.ADVANCED, SkillDirection.TEACH, PreferredMode.FREE, BigDecimal.valueOf(30.0)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.updateSkill(user.getEmail(), skillId, request)
        );
    }

    @Test
    void updateSkill_fail_wrongUser() {
        UUID skillId = UUID.randomUUID();
        SkillRequest request = new SkillRequest(
                "Java", ProficiencyLevel.ADVANCED, SkillDirection.TEACH, PreferredMode.FREE, BigDecimal.valueOf(30.0)
        );

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        UserSkill existingSkill = new UserSkill();
        existingSkill.setId(skillId);
        existingSkill.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(existingSkill));

        assertThrows(IllegalArgumentException.class, () ->
                skillService.updateSkill(user.getEmail(), skillId, request)
        );
    }

    @Test
    void deleteSkill_success() {
        UUID skillId = UUID.randomUUID();
        UserSkill existingSkill = new UserSkill();
        existingSkill.setId(skillId);
        existingSkill.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(existingSkill));
        doNothing().when(skillRepository).delete(existingSkill);

        skillService.deleteSkill(user.getEmail(), skillId);

        verify(skillRepository, times(1)).delete(existingSkill);
    }

    @Test
    void deleteSkill_userNotFound() {
        UUID skillId = UUID.randomUUID();
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.deleteSkill("unknown@test.com", skillId)
        );
    }

    @Test
    void deleteSkill_skillNotFound() {
        UUID skillId = UUID.randomUUID();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.deleteSkill(user.getEmail(), skillId)
        );
    }

    @Test
    void deleteSkill_fail_wrongUser() {
        UUID skillId = UUID.randomUUID();
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        UserSkill existingSkill = new UserSkill();
        existingSkill.setId(skillId);
        existingSkill.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(existingSkill));

        assertThrows(IllegalArgumentException.class, () ->
                skillService.deleteSkill(user.getEmail(), skillId)
        );
    }

    @Test
    void getMySkills_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        UserSkill skill = new UserSkill();
        skill.setId(UUID.randomUUID());
        skill.setUser(user);
        skill.setSkillName("Java");
        skill.setProficiency(ProficiencyLevel.ADVANCED);
        skill.setDirection(SkillDirection.TEACH);
        skill.setPreferredMode(PreferredMode.SWAP);
        skill.setHourlyRate(BigDecimal.valueOf(40.0));

        when(skillRepository.findByUserId(userId)).thenReturn(List.of(skill));

        List<SkillResponse> results = skillService.getMySkills(user.getEmail());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Java", results.get(0).skillName());
    }

    @Test
    void getMySkills_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                skillService.getMySkills("unknown@test.com")
        );
    }
}
