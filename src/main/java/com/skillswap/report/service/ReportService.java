package com.skillswap.report.service;

import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.report.dto.ReportRequest;
import com.skillswap.report.dto.ReportResponse;
import com.skillswap.report.entity.Report;
import com.skillswap.report.enums.ReportStatus;
import com.skillswap.report.repository.ReportRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public ReportResponse submitReport(String reporterEmail, ReportRequest request) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User reported = userRepository.findById(request.reportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reported user not found"));

        if (reporter.getId().equals(reported.getId())) {
            throw new IllegalArgumentException("Cannot report yourself");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reported);
        report.setReason(request.reason());
        report.setDescription(request.description());
        report = reportRepository.save(report);
        return toResponse(report);
    }

    public List<ReportResponse> getMyReports(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return reportRepository.findByReporterId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ReportResponse updateReportStatus(UUID reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setStatus(status);
        report = reportRepository.save(report);
        return toResponse(report);
    }

    private ReportResponse toResponse(Report r) {
        return new ReportResponse(r.getId(), r.getReporter().getId(),
                r.getReporter().getFirstName() + " " + r.getReporter().getLastName(),
                r.getReportedUser().getId(),
                r.getReportedUser().getFirstName() + " " + r.getReportedUser().getLastName(),
                r.getReason(), r.getDescription(), r.getStatus(), r.getCreatedAt());
    }
}
