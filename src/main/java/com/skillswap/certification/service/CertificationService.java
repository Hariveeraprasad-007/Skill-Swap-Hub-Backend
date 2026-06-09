package com.skillswap.certification.service;

import com.skillswap.certification.dto.CertificationRequest;
import com.skillswap.certification.dto.CertificationResponse;
import com.skillswap.certification.entity.Certification;
import com.skillswap.certification.repository.CertificationRepository;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserRepository userRepository;

    public CertificationService(CertificationRepository certificationRepository, UserRepository userRepository) {
        this.certificationRepository = certificationRepository;
        this.userRepository = userRepository;
    }

    public CertificationResponse add(String email, CertificationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Certification cert = new Certification();
        cert.setUser(user);
        cert.setName(request.name());
        cert.setIssuingOrganization(request.issuingOrganization());
        cert.setIssueDate(request.issueDate());
        cert.setExpiryDate(request.expiryDate());
        cert.setCredentialId(request.credentialId());
        cert.setCredentialUrl(request.credentialUrl());

        cert = certificationRepository.save(cert);
        return toResponse(cert);
    }

    public CertificationResponse update(String email, UUID certId, CertificationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));

        if (!cert.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own certifications");
        }

        cert.setName(request.name());
        cert.setIssuingOrganization(request.issuingOrganization());
        cert.setIssueDate(request.issueDate());
        cert.setExpiryDate(request.expiryDate());
        cert.setCredentialId(request.credentialId());
        cert.setCredentialUrl(request.credentialUrl());

        cert = certificationRepository.save(cert);
        return toResponse(cert);
    }

    public void delete(String email, UUID certId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Certification cert = certificationRepository.findById(certId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));

        if (!cert.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own certifications");
        }

        certificationRepository.delete(cert);
    }

    public List<CertificationResponse> getMyCertifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return certificationRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).toList();
    }

    private CertificationResponse toResponse(Certification cert) {
        return new CertificationResponse(
                cert.getId(), cert.getName(), cert.getIssuingOrganization(),
                cert.getIssueDate(), cert.getExpiryDate(),
                cert.getCredentialId(), cert.getCredentialUrl()
        );
    }
}
