package com.skillswap.report.service;

import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.report.dto.ReportRequest;
import com.skillswap.report.dto.ReportResponse;
import com.skillswap.report.entity.Report;
import com.skillswap.report.enums.ReportStatus;
import com.skillswap.report.repository.ReportRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private User reporter;
    private User reported;
    private UUID reporterId;
    private UUID reportedId;

    @BeforeEach
    void setUp() {
        reporterId = UUID.randomUUID();
        reportedId = UUID.randomUUID();

        reporter = new User();
        reporter.setId(reporterId);
        reporter.setEmail("reporter@test.com");
        reporter.setFirstName("John");
        reporter.setLastName("Reporter");

        reported = new User();
        reported.setId(reportedId);
        reported.setEmail("reported@test.com");
        reported.setFirstName("Spam");
        reported.setLastName("User");
    }

    @Test
    void submitReport_success() {
        ReportRequest request = new ReportRequest(reportedId, "SPAM", "User is spamming chat");

        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        when(userRepository.findById(reportedId)).thenReturn(Optional.of(reported));

        Report savedReport = new Report();
        savedReport.setId(UUID.randomUUID());
        savedReport.setReporter(reporter);
        savedReport.setReportedUser(reported);
        savedReport.setReason("SPAM");
        savedReport.setDescription("User is spamming chat");
        savedReport.setStatus(ReportStatus.PENDING);

        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

        ReportResponse response = reportService.submitReport(reporter.getEmail(), request);

        assertNotNull(response);
        assertEquals("SPAM", response.reason());
        assertEquals(ReportStatus.PENDING, response.status());
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void submitReport_fail_selfReport() {
        ReportRequest request = new ReportRequest(reporterId, "SPAM", "Reporting myself");

        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));

        assertThrows(IllegalArgumentException.class, () ->
                reportService.submitReport(reporter.getEmail(), request)
        );
    }

    @Test
    void submitReport_fail_reporterNotFound() {
        ReportRequest request = new ReportRequest(reportedId, "SPAM", "User is spamming chat");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                reportService.submitReport("unknown@test.com", request)
        );
    }

    @Test
    void submitReport_fail_reportedNotFound() {
        ReportRequest request = new ReportRequest(reportedId, "SPAM", "User is spamming chat");
        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        when(userRepository.findById(reportedId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                reportService.submitReport(reporter.getEmail(), request)
        );
    }

    @Test
    void getMyReports_success() {
        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        Report r = new Report();
        r.setId(UUID.randomUUID());
        r.setReporter(reporter);
        r.setReportedUser(reported);
        r.setReason("SPAM");
        r.setDescription("Description");
        r.setStatus(ReportStatus.PENDING);

        when(reportRepository.findByReporterId(reporterId)).thenReturn(java.util.List.of(r));

        java.util.List<ReportResponse> results = reportService.getMyReports(reporter.getEmail());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("SPAM", results.get(0).reason());
    }

    @Test
    void getMyReports_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                reportService.getMyReports("unknown@test.com")
        );
    }

    @Test
    void getAllReports_success() {
        Report r = new Report();
        r.setId(UUID.randomUUID());
        r.setReporter(reporter);
        r.setReportedUser(reported);
        r.setReason("INAPPROPRIATE");
        r.setDescription("Description");
        r.setStatus(ReportStatus.REVIEWED);

        when(reportRepository.findAll()).thenReturn(java.util.List.of(r));

        java.util.List<ReportResponse> results = reportService.getAllReports();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("INAPPROPRIATE", results.get(0).reason());
        assertEquals(ReportStatus.REVIEWED, results.get(0).status());
    }

    @Test
    void updateReportStatus_success() {
        UUID reportId = UUID.randomUUID();
        Report r = new Report();
        r.setId(reportId);
        r.setReporter(reporter);
        r.setReportedUser(reported);
        r.setReason("SPAM");
        r.setDescription("Description");
        r.setStatus(ReportStatus.PENDING);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(r));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportService.updateReportStatus(reportId, ReportStatus.REVIEWED);

        assertNotNull(response);
        assertEquals(ReportStatus.REVIEWED, response.status());
    }

    @Test
    void updateReportStatus_fail_notFound() {
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                reportService.updateReportStatus(reportId, ReportStatus.REVIEWED)
        );
    }
}
