package com.skillswap.certification.service;

import com.skillswap.certification.dto.CertificationRequest;
import com.skillswap.certification.dto.CertificationResponse;
import com.skillswap.certification.entity.Certification;
import com.skillswap.certification.repository.CertificationRepository;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

    @Mock
    private CertificationRepository certificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CertificationService certificationService;

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
    void addCertification_success() {
        CertificationRequest request = new CertificationRequest(
                "AWS Certified Solutions Architect", "Amazon Web Services",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                "AWS-12345", "https://aws.amazon.com/cert/12345"
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Certification saved = new Certification();
        saved.setId(UUID.randomUUID());
        saved.setUser(user);
        saved.setName("AWS Certified Solutions Architect");
        saved.setIssuingOrganization("Amazon Web Services");
        saved.setIssueDate(LocalDate.of(2025, 1, 1));
        saved.setExpiryDate(LocalDate.of(2028, 1, 1));
        saved.setCredentialId("AWS-12345");
        saved.setCredentialUrl("https://aws.amazon.com/cert/12345");

        when(certificationRepository.save(any(Certification.class))).thenReturn(saved);

        CertificationResponse response = certificationService.add(user.getEmail(), request);

        assertNotNull(response);
        assertEquals("AWS Certified Solutions Architect", response.name());
        assertEquals("Amazon Web Services", response.issuingOrganization());
        assertEquals("AWS-12345", response.credentialId());
    }

    @Test
    void addCertification_userNotFound() {
        CertificationRequest request = new CertificationRequest(
                "AWS Certified Solutions Architect", "Amazon Web Services",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                "AWS-12345", "https://aws.amazon.com/cert/12345"
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                certificationService.add("unknown@test.com", request)
        );
    }

    @Test
    void updateCertification_success() {
        UUID certId = UUID.randomUUID();
        CertificationRequest request = new CertificationRequest(
                "AWS Solutions Architect - Pro", "Amazon Web Services",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                "AWS-12345-PRO", "https://aws.amazon.com/cert/12345-PRO"
        );

        Certification existing = new Certification();
        existing.setId(certId);
        existing.setUser(user);
        existing.setName("AWS Certified Solutions Architect");
        existing.setIssuingOrganization("Amazon Web Services");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(certificationRepository.findById(certId)).thenReturn(Optional.of(existing));
        when(certificationRepository.save(any(Certification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CertificationResponse response = certificationService.update(user.getEmail(), certId, request);

        assertNotNull(response);
        assertEquals("AWS Solutions Architect - Pro", response.name());
        assertEquals("AWS-12345-PRO", response.credentialId());
    }

    @Test
    void updateCertification_userNotFound() {
        UUID certId = UUID.randomUUID();
        CertificationRequest request = new CertificationRequest(
                "AWS Solutions Architect - Pro", "Amazon Web Services",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                "AWS-12345-PRO", "https://aws.amazon.com/cert/12345-PRO"
        );
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                certificationService.update("unknown@test.com", certId, request)
        );
    }

    @Test
    void updateCertification_notFound() {
        UUID certId = UUID.randomUUID();
        CertificationRequest request = new CertificationRequest(
                "AWS Solutions Architect - Pro", "Amazon Web Services",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                "AWS-12345-PRO", "https://aws.amazon.com/cert/12345-PRO"
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(certificationRepository.findById(certId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                certificationService.update(user.getEmail(), certId, request)
        );
    }

    @Test
    void updateCertification_fail_wrongUser() {
        UUID certId = UUID.randomUUID();
        CertificationRequest request = new CertificationRequest(
                "AWS Solutions Architect - Pro", "Amazon Web Services",
                LocalDate.of(2025, 1, 1), LocalDate.of(2028, 1, 1),
                "AWS-12345-PRO", "https://aws.amazon.com/cert/12345-PRO"
        );

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        Certification existing = new Certification();
        existing.setId(certId);
        existing.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(certificationRepository.findById(certId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                certificationService.update(user.getEmail(), certId, request)
        );
    }

    @Test
    void deleteCertification_success() {
        UUID certId = UUID.randomUUID();
        Certification existing = new Certification();
        existing.setId(certId);
        existing.setUser(user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(certificationRepository.findById(certId)).thenReturn(Optional.of(existing));
        doNothing().when(certificationRepository).delete(existing);

        certificationService.delete(user.getEmail(), certId);

        verify(certificationRepository, times(1)).delete(existing);
    }

    @Test
    void deleteCertification_userNotFound() {
        UUID certId = UUID.randomUUID();
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                certificationService.delete("unknown@test.com", certId)
        );
    }

    @Test
    void deleteCertification_notFound() {
        UUID certId = UUID.randomUUID();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(certificationRepository.findById(certId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                certificationService.delete(user.getEmail(), certId)
        );
    }

    @Test
    void deleteCertification_fail_wrongUser() {
        UUID certId = UUID.randomUUID();
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        Certification existing = new Certification();
        existing.setId(certId);
        existing.setUser(otherUser);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(certificationRepository.findById(certId)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
                certificationService.delete(user.getEmail(), certId)
        );
    }

    @Test
    void getMyCertifications_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Certification cert = new Certification();
        cert.setId(UUID.randomUUID());
        cert.setUser(user);
        cert.setName("CKA");
        cert.setIssuingOrganization("CNCF");

        when(certificationRepository.findByUserId(userId)).thenReturn(List.of(cert));

        List<CertificationResponse> results = certificationService.getMyCertifications(user.getEmail());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("CKA", results.get(0).name());
    }

    @Test
    void getMyCertifications_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                certificationService.getMyCertifications("unknown@test.com")
        );
    }
}
