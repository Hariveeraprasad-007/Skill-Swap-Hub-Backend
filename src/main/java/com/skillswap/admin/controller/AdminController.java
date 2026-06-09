package com.skillswap.admin.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.report.dto.ReportResponse;
import com.skillswap.report.enums.ReportStatus;
import com.skillswap.report.service.ReportService;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin-only management endpoints")
public class AdminController {

    private final UserRepository userRepository;
    private final ReportService reportService;

    public AdminController(UserRepository userRepository, ReportService reportService) {
        this.userRepository = userRepository;
        this.reportService = reportService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<Page<User>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved",
                userRepository.findAll(PageRequest.of(page, size))));
    }

    @PutMapping("/users/{id}/ban")
    @Operation(summary = "Ban or unban a user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleBan(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(
                user.isActive() ? "User unbanned" : "User banned",
                Map.of("userId", user.getId(), "isActive", user.isActive())));
    }

    @GetMapping("/reports")
    @Operation(summary = "List all reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> listReports() {
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved", reportService.getAllReports()));
    }

    @PutMapping("/reports/{id}/status")
    @Operation(summary = "Update report status")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReportStatus(
            @PathVariable UUID id, @RequestParam ReportStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Report updated", reportService.updateReportStatus(id, status)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform-wide statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(User::isActive).count();
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved", Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers
        )));
    }
}
