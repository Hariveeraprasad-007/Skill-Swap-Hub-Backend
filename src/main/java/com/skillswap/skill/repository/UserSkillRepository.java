package com.skillswap.skill.repository;

import com.skillswap.skill.entity.UserSkill;
import com.skillswap.skill.enums.SkillDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    List<UserSkill> findByUserId(UUID userId);

    List<UserSkill> findByUserIdAndDirection(UUID userId, SkillDirection direction);

    boolean existsByUserIdAndSkillNameIgnoreCaseAndDirection(UUID userId, String skillName, SkillDirection direction);
}
