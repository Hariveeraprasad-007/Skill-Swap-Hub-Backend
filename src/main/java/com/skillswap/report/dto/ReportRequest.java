package com.skillswap.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReportRequest(
        @NotNull UUID reportedUserId,
        @NotBlank String reason,
        String description
) {}
