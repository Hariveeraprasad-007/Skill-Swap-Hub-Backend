package com.skillswap.experience.service;

import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.experience.dto.ExperienceRequest;
import com.skillswap.experience.dto.ExperienceResponse;
import com.skillswap.experience.entity.Experience;
import com.skillswap.experience.repository.ExperienceRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    public ExperienceService(ExperienceRepository experienceRepository, UserRepository userRepository) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
    }

    public ExperienceResponse add(String email, ExperienceRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Experience exp = new Experience();
        exp.setUser(user);
        exp.setTitle(request.title());
        exp.setOrganization(request.organization());
        exp.setDescription(request.description());
        exp.setStartDate(request.startDate());
        exp.setEndDate(request.endDate());
        exp.setType(request.type());
        exp = experienceRepository.save(exp);
        return toResponse(exp);
    }

    public ExperienceResponse update(String email, UUID expId, ExperienceRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Experience exp = experienceRepository.findById(expId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        if (!exp.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own experience");
        }
        exp.setTitle(request.title());
        exp.setOrganization(request.organization());
        exp.setDescription(request.description());
        exp.setStartDate(request.startDate());
        exp.setEndDate(request.endDate());
        exp.setType(request.type());
        exp = experienceRepository.save(exp);
        return toResponse(exp);
    }

    public void delete(String email, UUID expId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Experience exp = experienceRepository.findById(expId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        if (!exp.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own experience");
        }
        experienceRepository.delete(exp);
    }

    public List<ExperienceResponse> getMyExperiences(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return experienceRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    private ExperienceResponse toResponse(Experience exp) {
        return new ExperienceResponse(exp.getId(), exp.getTitle(), exp.getOrganization(),
                exp.getDescription(), exp.getStartDate(), exp.getEndDate(), exp.getType());
    }
}
