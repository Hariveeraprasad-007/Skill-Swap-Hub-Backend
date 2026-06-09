package com.skillswap.user.service;

import com.skillswap.certification.dto.CertificationResponse;
import com.skillswap.certification.repository.CertificationRepository;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.experience.dto.ExperienceResponse;
import com.skillswap.experience.repository.ExperienceRepository;
import com.skillswap.skill.dto.SkillResponse;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.skill.repository.UserSkillRepository;
import com.skillswap.user.dto.UserProfileResponse;
import com.skillswap.user.dto.UserProfileUpdateRequest;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository skillRepository;
    private final CertificationRepository certificationRepository;
    private final ExperienceRepository experienceRepository;

    public UserService(UserRepository userRepository,
                       UserSkillRepository skillRepository,
                       CertificationRepository certificationRepository,
                       ExperienceRepository experienceRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.certificationRepository = certificationRepository;
        this.experienceRepository = experienceRepository;
    }

    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildProfile(user);
    }

    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildProfile(user);
    }

    public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl());
        if (request.githubUrl() != null) user.setGithubUrl(request.githubUrl());
        if (request.linkedinUrl() != null) user.setLinkedinUrl(request.linkedinUrl());
        if (request.leetcodeUrl() != null) user.setLeetcodeUrl(request.leetcodeUrl());
        if (request.codeforcesUrl() != null) user.setCodeforcesUrl(request.codeforcesUrl());

        user = userRepository.save(user);
        return buildProfile(user);
    }

    public List<UserProfileResponse> searchBySkill(String skill, SkillDirection direction) {
        return userRepository.searchBySkill(skill, direction)
                .stream().map(this::buildProfile).toList();
    }

    private UserProfileResponse buildProfile(User user) {
        List<SkillResponse> skills = skillRepository.findByUserId(user.getId()).stream()
                .map(s -> new SkillResponse(s.getId(), s.getSkillName(), s.getProficiency(),
                        s.getDirection(), s.getPreferredMode(), s.getHourlyRate()))
                .toList();

        List<CertificationResponse> certs = certificationRepository.findByUserId(user.getId()).stream()
                .map(c -> new CertificationResponse(c.getId(), c.getName(), c.getIssuingOrganization(),
                        c.getIssueDate(), c.getExpiryDate(), c.getCredentialId(), c.getCredentialUrl()))
                .toList();

        List<ExperienceResponse> exps = experienceRepository.findByUserId(user.getId()).stream()
                .map(e -> new ExperienceResponse(e.getId(), e.getTitle(), e.getOrganization(),
                        e.getDescription(), e.getStartDate(), e.getEndDate(), e.getType()))
                .toList();

        return new UserProfileResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getBio(), user.getProfilePictureUrl(),
                user.getGithubUrl(), user.getLinkedinUrl(),
                user.getLeetcodeUrl(), user.getCodeforcesUrl(),
                user.getRole().name(), user.getAverageRating(), user.getTotalSessions(),
                skills, certs, exps, user.getCreatedAt()
        );
    }
}
