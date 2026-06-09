package com.skillswap.availability.controller;

import com.skillswap.availability.dto.AvailabilityRequest;
import com.skillswap.availability.dto.AvailabilityResponse;
import com.skillswap.availability.service.AvailabilityService;
import com.skillswap.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availabilities")
@Tag(name = "Availability", description = "Manage your free-time teaching slots")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @Operation(summary = "Add an availability slot")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> add(
            Authentication auth, @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability added", availabilityService.add(auth.getName(), request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an availability slot")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable UUID id) {
        availabilityService.delete(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Availability deleted"));
    }

    @GetMapping("/my")
    @Operation(summary = "List your availability slots")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getMyAvailabilities(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Availabilities retrieved", availabilityService.getMyAvailabilities(auth.getName())));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "View another user's availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getUserAvailabilities(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Availabilities retrieved", availabilityService.getUserAvailabilities(userId)));
    }
}
