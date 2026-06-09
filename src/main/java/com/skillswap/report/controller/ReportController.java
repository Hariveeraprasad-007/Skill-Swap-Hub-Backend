package com.skillswap.report.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.report.dto.ReportRequest;
import com.skillswap.report.dto.ReportResponse;
import com.skillswap.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Report inappropriate behavior")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Report a user")
    public ResponseEntity<ApiResponse<ReportResponse>> submit(
            Authentication auth, @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report submitted", reportService.submitReport(auth.getName(), request)));
    }

    @GetMapping("/my")
    @Operation(summary = "View my submitted reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyReports(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved", reportService.getMyReports(auth.getName())));
    }
}
