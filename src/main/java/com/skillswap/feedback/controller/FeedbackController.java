package com.skillswap.feedback.controller;

import com.skillswap.common.dto.ApiResponse;
import com.skillswap.feedback.dto.FeedbackRequest;
import com.skillswap.feedback.dto.FeedbackResponse;
import com.skillswap.feedback.service.FeedbackService;
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
@RequestMapping("/api/feedback")
@Tag(name = "Feedback", description = "Submit and view session reviews")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @Operation(summary = "Submit feedback for a completed session")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submit(
            Authentication auth, @Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted", feedbackService.submitFeedback(auth.getName(), request)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all reviews for a user")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getUserReviews(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", feedbackService.getUserReviews(userId)));
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get feedback for a specific session")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getSessionFeedback(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved", feedbackService.getSessionFeedback(sessionId)));
    }
}
