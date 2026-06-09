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
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User reviewer;
    private User reviewee;
    private Session session;
    private UUID sessionId;
    private UUID reviewerId;
    private UUID revieweeId;

    @BeforeEach
    void setUp() {
        reviewerId = UUID.randomUUID();
        revieweeId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        reviewer = new User();
        reviewer.setId(reviewerId);
        reviewer.setEmail("reviewer@test.com");
        reviewer.setFirstName("Reviewer");
        reviewer.setLastName("User");

        reviewee = new User();
        reviewee.setId(revieweeId);
        reviewee.setEmail("reviewee@test.com");
        reviewee.setFirstName("Reviewee");
        reviewee.setLastName("User");

        session = new Session();
        session.setId(sessionId);
        session.setTeacher(reviewee); // reviewer is student, reviewee is teacher
        session.setStudent(reviewer);
        session.setStatus(SessionStatus.COMPLETED);
    }

    @Test
    void submitFeedback_success() {
        FeedbackRequest request = new FeedbackRequest(sessionId, 5, "Great lesson!");

        when(userRepository.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(feedbackRepository.existsBySessionIdAndReviewerId(sessionId, reviewerId)).thenReturn(false);
        when(feedbackRepository.calculateAverageRating(revieweeId)).thenReturn(4.8);

        Feedback saved = new Feedback();
        saved.setId(UUID.randomUUID());
        saved.setSession(session);
        saved.setReviewer(reviewer);
        saved.setReviewee(reviewee);
        saved.setRating(5);
        saved.setComment("Great lesson!");

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(saved);

        FeedbackResponse response = feedbackService.submitFeedback(reviewer.getEmail(), request);

        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Great lesson!", response.comment());
        verify(userRepository, times(1)).save(reviewee);
        assertEquals(4.8, reviewee.getAverageRating());
    }

    @Test
    void submitFeedback_fail_sessionNotCompleted() {
        FeedbackRequest request = new FeedbackRequest(sessionId, 5, "Nice");
        session.setStatus(SessionStatus.ACCEPTED); // not completed

        when(userRepository.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                feedbackService.submitFeedback(reviewer.getEmail(), request)
        );
    }

    @Test
    void submitFeedback_fail_notParticipant() {
        FeedbackRequest request = new FeedbackRequest(sessionId, 5, "Nice");
        User externalUser = new User();
        externalUser.setId(UUID.randomUUID());
        externalUser.setEmail("external@test.com");

        when(userRepository.findByEmail(externalUser.getEmail())).thenReturn(Optional.of(externalUser));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                feedbackService.submitFeedback(externalUser.getEmail(), request)
        );
    }

    @Test
    void submitFeedback_fail_duplicateFeedback() {
        FeedbackRequest request = new FeedbackRequest(sessionId, 5, "Nice");

        when(userRepository.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(feedbackRepository.existsBySessionIdAndReviewerId(sessionId, reviewerId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                feedbackService.submitFeedback(reviewer.getEmail(), request)
        );
    }

    @Test
    void submitFeedback_fail_reviewerNotFound() {
        FeedbackRequest request = new FeedbackRequest(sessionId, 5, "Great!");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                feedbackService.submitFeedback("unknown@test.com", request)
        );
    }

    @Test
    void submitFeedback_fail_sessionNotFound() {
        FeedbackRequest request = new FeedbackRequest(sessionId, 5, "Great!");
        when(userRepository.findByEmail(reviewer.getEmail())).thenReturn(Optional.of(reviewer));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                feedbackService.submitFeedback(reviewer.getEmail(), request)
        );
    }

    @Test
    void getUserReviews_success() {
        Feedback f = new Feedback();
        f.setId(UUID.randomUUID());
        f.setSession(session);
        f.setReviewer(reviewer);
        f.setReviewee(reviewee);
        f.setRating(4);
        f.setComment("Good job");

        when(feedbackRepository.findByRevieweeId(revieweeId)).thenReturn(java.util.List.of(f));

        java.util.List<FeedbackResponse> results = feedbackService.getUserReviews(revieweeId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Good job", results.get(0).comment());
    }

    @Test
    void getSessionFeedback_success() {
        Feedback f = new Feedback();
        f.setId(UUID.randomUUID());
        f.setSession(session);
        f.setReviewer(reviewer);
        f.setReviewee(reviewee);
        f.setRating(5);
        f.setComment("Amazing!");

        when(feedbackRepository.findBySessionId(sessionId)).thenReturn(java.util.List.of(f));

        java.util.List<FeedbackResponse> results = feedbackService.getSessionFeedback(sessionId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).rating());
    }
}
