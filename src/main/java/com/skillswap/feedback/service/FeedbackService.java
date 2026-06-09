package com.skillswap.feedback.service;

import com.skillswap.common.exception.DuplicateResourceException;
import com.skillswap.common.exception.InvalidOperationException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.feedback.dto.FeedbackRequest;
import com.skillswap.feedback.dto.FeedbackResponse;
import com.skillswap.feedback.entity.Feedback;
import com.skillswap.feedback.repository.FeedbackRepository;
import com.skillswap.session.entity.Session;
import com.skillswap.session.enums.SessionStatus;
import com.skillswap.session.repository.SessionRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           SessionRepository sessionRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeedbackResponse submitFeedback(String reviewerEmail, FeedbackRequest request) {
        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Session session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Session must be completed
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new InvalidOperationException("Can only review completed sessions");
        }

        // Reviewer must be a participant
        boolean isTeacher = session.getTeacher().getId().equals(reviewer.getId());
        boolean isStudent = session.getStudent().getId().equals(reviewer.getId());
        if (!isTeacher && !isStudent) {
            throw new InvalidOperationException("You are not a participant of this session");
        }

        // Check duplicate review
        if (feedbackRepository.existsBySessionIdAndReviewerId(session.getId(), reviewer.getId())) {
            throw new DuplicateResourceException("You have already reviewed this session");
        }

        // Determine reviewee (the other participant)
        User reviewee = isTeacher ? session.getStudent() : session.getTeacher();

        Feedback feedback = new Feedback();
        feedback.setSession(session);
        feedback.setReviewer(reviewer);
        feedback.setReviewee(reviewee);
        feedback.setRating(request.rating());
        feedback.setComment(request.comment());
        feedback = feedbackRepository.save(feedback);

        // Update reviewee's average rating
        Double avgRating = feedbackRepository.calculateAverageRating(reviewee.getId());
        if (avgRating != null) {
            reviewee.setAverageRating(avgRating);
            userRepository.save(reviewee);
        }

        return toResponse(feedback);
    }

    public List<FeedbackResponse> getUserReviews(UUID userId) {
        return feedbackRepository.findByRevieweeId(userId)
                .stream().map(this::toResponse).toList();
    }

    public List<FeedbackResponse> getSessionFeedback(UUID sessionId) {
        return feedbackRepository.findBySessionId(sessionId)
                .stream().map(this::toResponse).toList();
    }

    private FeedbackResponse toResponse(Feedback f) {
        return new FeedbackResponse(
                f.getId(), f.getSession().getId(),
                f.getReviewer().getId(), f.getReviewer().getFirstName() + " " + f.getReviewer().getLastName(),
                f.getReviewee().getId(), f.getReviewee().getFirstName() + " " + f.getReviewee().getLastName(),
                f.getRating(), f.getComment(), f.getCreatedAt()
        );
    }
}
