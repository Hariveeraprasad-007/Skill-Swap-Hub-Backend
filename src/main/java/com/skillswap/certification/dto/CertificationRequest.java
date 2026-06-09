package com.skillswap.certification.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CertificationRequest(
        @NotBlank(message = "Certification name is required")
        String name,
        @NotBlank(message = "Issuing organization is required")
        String issuingOrganization,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialId,
        String credentialUrl
) {}
