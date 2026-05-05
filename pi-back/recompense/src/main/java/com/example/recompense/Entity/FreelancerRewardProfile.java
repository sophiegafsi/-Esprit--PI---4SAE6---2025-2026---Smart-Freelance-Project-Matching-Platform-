package com.example.recompense.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "freelancer_reward_profile")
@Data
public class FreelancerRewardProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail;

    private String userName;

    private Double averageScore = 0.0;

    private Integer latestScore = 0;

    private Integer totalEvaluations = 0;

    private Integer positiveEvaluations = 0;

    private Integer completedProjects = 0;

    private Integer totalPoints = 0;

    private String currentLevel = "Niveau 1 - New Freelancer";

    private String currentScoreBadge;

    private String currentPointsBadge;

    private Integer totalBadgesAwarded = 0;

    private LocalDateTime lastEvaluationAt;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
