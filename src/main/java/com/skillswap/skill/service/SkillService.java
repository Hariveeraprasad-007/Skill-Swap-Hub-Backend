package com.skillswap.skill.service;

import com.skillswap.common.exception.DuplicateResourceException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.skill.dto.SkillRequest;
import com.skillswap.skill.dto.SkillResponse;
import com.skillswap.skill.entity.UserSkill;
import com.skillswap.skill.enums.PreferredMode;
import com.skillswap.skill.repository.UserSkillRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SkillService {

    private final UserSkillRepository skillRepository;
    private final UserRepository userRepository;

    public SkillService(UserSkillRepository skillRepository, UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    public SkillResponse addSkill(String email, SkillRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (skillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                user.getId(), request.skillName(), request.direction())) {
            throw new DuplicateResourceException(
                    "You already have '" + request.skillName() + "' listed as " + request.direction());
        }

        UserSkill skill = new UserSkill();
        skill.setUser(user);
        skill.setSkillName(request.skillName());
        skill.setProficiency(request.proficiency());
        skill.setDirection(request.direction());
        skill.setPreferredMode(request.preferredMode() != null ? request.preferredMode() : PreferredMode.ANY);
        skill.setHourlyRate(request.hourlyRate());

        skill = skillRepository.save(skill);
        return toResponse(skill);
    }

    public SkillResponse updateSkill(String email, UUID skillId, SkillRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (!skill.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own skills");
        }

        skill.setSkillName(request.skillName());
        skill.setProficiency(request.proficiency());
        skill.setDirection(request.direction());
        skill.setPreferredMode(request.preferredMode() != null ? request.preferredMode() : PreferredMode.ANY);
        skill.setHourlyRate(request.hourlyRate());

        skill = skillRepository.save(skill);
        return toResponse(skill);
    }

    public void deleteSkill(String email, UUID skillId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (!skill.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own skills");
        }

        skillRepository.delete(skill);
    }

    public List<SkillResponse> getMySkills(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return skillRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SkillResponse toResponse(UserSkill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getSkillName(),
                skill.getProficiency(),
                skill.getDirection(),
                skill.getPreferredMode(),
                skill.getHourlyRate()
        );
    }
}
