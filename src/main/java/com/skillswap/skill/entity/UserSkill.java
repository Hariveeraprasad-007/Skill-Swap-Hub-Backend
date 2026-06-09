package com.skillswap.skill.entity;

import com.skillswap.skill.enums.PreferredMode;
import com.skillswap.skill.enums.ProficiencyLevel;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String skillName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel proficiency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillDirection direction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferredMode preferredMode = PreferredMode.ANY;

    private BigDecimal hourlyRate;

    // ── Getters and Setters ──

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public ProficiencyLevel getProficiency() { return proficiency; }
    public void setProficiency(ProficiencyLevel proficiency) { this.proficiency = proficiency; }

    public SkillDirection getDirection() { return direction; }
    public void setDirection(SkillDirection direction) { this.direction = direction; }

    public PreferredMode getPreferredMode() { return preferredMode; }
    public void setPreferredMode(PreferredMode preferredMode) { this.preferredMode = preferredMode; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}
