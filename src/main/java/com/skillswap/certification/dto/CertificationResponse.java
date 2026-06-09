package com.skillswap.certification.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CertificationResponse(
        UUID id,
        String name,
        String issuingOrganization,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialId,
        String credentialUrl
) {}
