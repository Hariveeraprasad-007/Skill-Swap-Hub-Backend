package com.skillswap.availability.service;

import com.skillswap.availability.dto.AvailabilityRequest;
import com.skillswap.availability.dto.AvailabilityResponse;
import com.skillswap.availability.entity.Availability;
import com.skillswap.availability.repository.AvailabilityRepository;
import com.skillswap.common.exception.DuplicateResourceException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository, UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    public AvailabilityResponse add(String email, AvailabilityRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.endTime().isBefore(request.startTime()) || request.endTime().equals(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check for overlapping availability slots on the same day
        List<Availability> overlaps = availabilityRepository.findOverlapping(
                user.getId(), request.dayOfWeek(), request.startTime(), request.endTime());
        if (!overlaps.isEmpty()) {
            throw new DuplicateResourceException("Overlapping availability slot exists on this day");
        }

        Availability availability = new Availability();
        availability.setUser(user);
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setTimezone(request.timezone());

        availability = availabilityRepository.save(availability);
        return toResponse(availability);
    }

    public void delete(String email, UUID availabilityId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Availability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));
        if (!availability.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own availability slots");
        }
        availabilityRepository.delete(availability);
    }

    public List<AvailabilityResponse> getMyAvailabilities(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return availabilityRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<AvailabilityResponse> getUserAvailabilities(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        return availabilityRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    private AvailabilityResponse toResponse(Availability a) {
        return new AvailabilityResponse(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.getTimezone());
    }
}
