package com.skillswap.session.repository;

import com.skillswap.session.entity.Session;
import com.skillswap.session.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Find sessions where the teacher has a conflicting time slot.
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.teacher.id = :teacherId
          AND s.status IN ('PENDING', 'ACCEPTED')
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    List<Session> findTeacherConflicts(@Param("teacherId") UUID teacherId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * Find sessions where the student has a conflicting time slot.
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.student.id = :studentId
          AND s.status IN ('PENDING', 'ACCEPTED')
          AND s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    List<Session> findStudentConflicts(@Param("studentId") UUID studentId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    @Query("""
        SELECT s FROM Session s
        WHERE (s.teacher.id = :userId OR s.student.id = :userId)
        ORDER BY s.startTime DESC
    """)
    List<Session> findByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT s FROM Session s
        WHERE (s.teacher.id = :userId OR s.student.id = :userId)
          AND s.status = :status
        ORDER BY s.startTime DESC
    """)
    List<Session> findByUserIdAndStatus(@Param("userId") UUID userId,
                                        @Param("status") SessionStatus status);

    @Query("SELECT COUNT(s) FROM Session s WHERE (s.teacher.id = :userId OR s.student.id = :userId) AND s.status = 'COMPLETED'")
    int countCompletedByUserId(@Param("userId") UUID userId);
}
