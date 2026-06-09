package com.skillswap.feedback.repository;

import com.skillswap.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findByRevieweeId(UUID revieweeId);

    List<Feedback> findBySessionId(UUID sessionId);

    boolean existsBySessionIdAndReviewerId(UUID sessionId, UUID reviewerId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.reviewee.id = :userId")
    Double calculateAverageRating(@Param("userId") UUID userId);
}
