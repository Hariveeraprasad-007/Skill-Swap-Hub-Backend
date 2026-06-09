package com.skillswap.availability.repository;

import com.skillswap.availability.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    List<Availability> findByUserId(UUID userId);

    @Query("""
        SELECT a FROM Availability a
        WHERE a.user.id = :userId
          AND a.dayOfWeek = :dayOfWeek
          AND a.startTime <= :startTime
          AND a.endTime >= :endTime
    """)
    List<Availability> findCoveringSlot(@Param("userId") UUID userId,
                                        @Param("dayOfWeek") int dayOfWeek,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime);

    @Query("""
        SELECT a FROM Availability a
        WHERE a.user.id = :userId
          AND a.dayOfWeek = :dayOfWeek
          AND a.startTime < :endTime
          AND a.endTime > :startTime
    """)
    List<Availability> findOverlapping(@Param("userId") UUID userId,
                                       @Param("dayOfWeek") int dayOfWeek,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
}
