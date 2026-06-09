package com.skillswap.certification.controller;

import com.skillswap.certification.dto.CertificationRequest;
import com.skillswap.certification.dto.CertificationResponse;
import com.skillswap.certification.service.CertificationService;
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
@RequestMapping("/api/certifications")
@Tag(name = "Certifications", description = "Manage your certifications")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping
    @Operation(summary = "Add a certification")
    public ResponseEntity<ApiResponse<CertificationResponse>> add(
            Authentication auth, @Valid @RequestBody CertificationRequest request) {
        CertificationResponse response = certificationService.add(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certification added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a certification")
    public ResponseEntity<ApiResponse<CertificationResponse>> update(
            Authentication auth, @PathVariable UUID id, @Valid @RequestBody CertificationRequest request) {
        CertificationResponse response = certificationService.update(auth.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Certification updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a certification")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable UUID id) {
        certificationService.delete(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("Certification deleted"));
    }

    @GetMapping("/my")
    @Operation(summary = "List your certifications")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> getMyCertifications(Authentication auth) {
        List<CertificationResponse> certs = certificationService.getMyCertifications(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Certifications retrieved", certs));
    }
}
