package com.skillswap.user.repository;

import com.skillswap.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN com.skillswap.skill.entity.UserSkill s ON s.user = u
        WHERE LOWER(s.skillName) LIKE LOWER(CONCAT('%', :skill, '%'))
          AND s.direction = :direction
          AND u.isActive = true
    """)
    List<User> searchBySkill(@Param("skill") String skill,
                             @Param("direction") com.skillswap.skill.enums.SkillDirection direction);

    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.averageRating DESC")
    List<User> findTopRatedUsers();
}
