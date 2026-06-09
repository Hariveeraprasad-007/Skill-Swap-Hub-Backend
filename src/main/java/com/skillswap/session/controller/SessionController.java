package com.skillswap.session.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.session.dto.SessionBookRequest;
import com.skillswap.session.dto.SessionResponse;
import com.skillswap.session.enums.SessionStatus;
import com.skillswap.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sessions", description = "Book, manage, and track teaching sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/book")
    @Operation(summary = "Book a session (runs conflict detection)")
    public ResponseEntity<ApiResponse<SessionResponse>> bookSession(
            Authentication auth, @Valid @RequestBody SessionBookRequest request) {
        SessionResponse response = sessionService.bookSession(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session booked successfully", response));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept a session request (teacher only)")
    public ResponseEntity<ApiResponse<SessionResponse>> acceptSession(
            Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session accepted", sessionService.acceptSession(auth.getName(), id)));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a session request (teacher only)")
    public ResponseEntity<ApiResponse<SessionResponse>> rejectSession(
            Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session rejected", sessionService.rejectSession(auth.getName(), id)));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Mark session as completed")
    public ResponseEntity<ApiResponse<SessionResponse>> completeSession(
            Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session completed", sessionService.completeSession(auth.getName(), id)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a session with reason")
    public ResponseEntity<ApiResponse<SessionResponse>> cancelSession(
            Authentication auth, @PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return ResponseEntity.ok(ApiResponse.success("Session cancelled", sessionService.cancelSession(auth.getName(), id, reason)));
    }

    @GetMapping("/my")
    @Operation(summary = "List my sessions (optional status filter)")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getMySessions(
            Authentication auth, @RequestParam(required = false) SessionStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Sessions retrieved", sessionService.getMySessions(auth.getName(), status)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session details")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Session retrieved", sessionService.getSession(id)));
    }
}
